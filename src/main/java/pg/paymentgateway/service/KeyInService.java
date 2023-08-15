package pg.paymentgateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pg.paymentgateway.dto.*;
import pg.paymentgateway.entity.ClientRequest;
import pg.paymentgateway.entity.Merchant;
import pg.paymentgateway.entity.Pay;
import pg.paymentgateway.entity.Van;
import pg.paymentgateway.exception.ForbiddenException;
import pg.paymentgateway.repository.ClientRequestRepository;
import pg.paymentgateway.repository.MerchantRepository;
import pg.paymentgateway.repository.PayRepository;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyInService {

    private final MerchantRepository merchantRepository;
    private final ClientRequestRepository clientRequestRepository;
    private final PayRepository payRepository;
    private final ObjectMapper objectMapper;

    private static final String INVALID_EXPIRE_DATE = "올바르지않은 유효기간입니다.";
    private static final String INVALID_BIRTHDAY = "올바르지않은 생년월일입니다.";
    private static final String INVALID_PASSWORD = "올바르지않은 비밀번호입니다.";
    private static final String FORBIDDEN_MERCHANT = "존재하지않은 가맹점 ID입니다.";
    private static final String INTERNAL_EXCEPTION = "내부 서버 오류입니다. 잠시 후 다시 시도해주세요.";
    private static final String DUPLICATION_ORDER_ID = "중복된 주문번호입니다.";
    private static final String OLD_KEYIN_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/pay/oldcert";
    private static final String NON_KEYIN_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/pay/noncert";
    private static final String RESULT_CODE = "0000";
    private static final String RESULT_MESSAGE = "정상 승인되었습니다.";

    @Transactional
    public Object oldCertification(ClientKeyInRequestDTO clientRequestDTO, String method) {

        // 유효기간 검증
        if(!this.validationExpireDate(clientRequestDTO.getExpireDate())){
            throw new IllegalArgumentException(INVALID_EXPIRE_DATE);
        }

        // 구인증(카유생비) 일 경우 추가 검증
        if ("old-keyIn".equals(method)) {
            // 생년월일 검증
            if(!this.validationUserInfo(clientRequestDTO.getUserInfo())){
                throw new IllegalArgumentException(INVALID_BIRTHDAY);
            }
            // 비밀번호 자릿수 검증
            if(!this.validationPassword(clientRequestDTO.getPassword())){
                throw new IllegalArgumentException(INVALID_PASSWORD);
            }
        }

        // TRANSACTION ID 생성
        String transactionId = UUID.randomUUID().toString();

        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantId(clientRequestDTO.getMerchantId()));

        if(merchant.isEmpty()){
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }else{
            // 가맹점 별 주문 ID 중복 검증
            Optional<ClientRequest> orderId = Optional.ofNullable(clientRequestRepository.findClientRequestByOrderIdAndMerchantId(clientRequestDTO.getOrderId(), clientRequestDTO.getMerchantId()));

            if(orderId.isPresent()){
                throw new IllegalArgumentException(DUPLICATION_ORDER_ID);
            }

            List<Van> vans = merchant.get().getVans();
            String vanId = "";
            Van van = null;

            for (Van findVan : vans) {
                if(findVan.getMerchant().getMerchantId().equals(merchant.get().getMerchantId()) && findVan.getMethod().equals(method)){
                    van = findVan;
                    break;
                }
            }

            //CLIENT REQUEST INSERT
            clientRequestRepository.save(setClientRequest(clientRequestDTO, van));

            // KSNET API CALL
            KsnetResponse ksnetResponse = null;

            ksnetResponse = callKsnetAPI(transactionId, clientRequestDTO, van, method);

            if(!"A0200".equals(ksnetResponse.getCode())){
                // API 응답 정상이 아닐 경우
                return new ErrorResultDTO().builder()
                        .errorCode("0500")
                        .errorMessage(INTERNAL_EXCEPTION)
                        .build();
            }else{
                // 정상 승인 응답이 아닐 경우(HTTP STATUS CODE -> 200인데 KSNET 응답 코드가 0000이 아닐 경우)
                if(!"0000".equals(ksnetResponse.getData().getRespCode())){
                    throw new IllegalArgumentException(ksnetResponse.getMessage());
                }else if("0000".equals(ksnetResponse.getData().getRespCode())){
                    // PAY INSERT
                    payRepository.save(setKeyInPay(transactionId, method, clientRequestDTO, ksnetResponse, van, merchant));
                }
            }
        }
        return new ClientResponseDTO().builder()
                .transactionId(transactionId)
                .orderId(clientRequestDTO.getOrderId())
                .orderName(clientRequestDTO.getOrderName())
                .resultCode(RESULT_CODE)
                .resultMessage(RESULT_MESSAGE)
                .build();
    }

    /**
     * KSNET API 요청 분기 처리
     * @param transactionId
     * @param clientRequest
     * @param van
     * @param method
     * @return
     */
    private KsnetResponse callKsnetAPI(String transactionId, ClientKeyInRequestDTO clientRequest, Van van, String method){
        KsnetResponse ksnetResponse = null;
        // 구인증 API
        if("old-keyIn".equals(method)){
            KsnetOldKeyInRequestDTO KsnetRequest = this.setKsnetOldKeyInRequest(transactionId, clientRequest, van);
            ksnetResponse = callOldKeyIn(KsnetRequest);
        }else if("non-keyIn".equals(method)){
            KsnetNonKeyInRequestDTO ksnetNonRequest = this.setKsnetNonKeyInRequest(transactionId, clientRequest, van);
            ksnetResponse = callNonKeyIn(ksnetNonRequest);

        }

        return ksnetResponse;
    }

    /**
     * 비인증 API CALL
     * @param requestDTO
     * @return
     */
    private KsnetResponse callNonKeyIn(KsnetNonKeyInRequestDTO requestDTO){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);
        headers.set("Authorization", "pgapi Mjk5OTE5OTk5MDpNQTAxOjNEMUVBOEVBRUM0NzA1MTFBMkIyNUVFMzQwRkI5ODQ4");
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestDTO), headers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("request => {}", entity.getBody());

        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(NON_KEYIN_URL, entity, KsnetResponse.class);

        log.info("response => {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 비인증 API 호출 실패");
            throw new RuntimeException("서버 통신 오류 잠시 후 다시 시도해주세요.");
        }

        return response.getBody();
    }

    /**
     * 구인증 API CALL
     * @param requestDTO
     * @return
     */
    private KsnetResponse callOldKeyIn(KsnetOldKeyInRequestDTO requestDTO){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);
        headers.set("Authorization", "pgapi Mjk5OTE5OTk5MDpNQTAxOjNEMUVBOEVBRUM0NzA1MTFBMkIyNUVFMzQwRkI5ODQ4");
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestDTO), headers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("request => {}", entity.getBody());

        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(OLD_KEYIN_URL, entity, KsnetResponse.class);

        log.info("response => {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 구인증 API 호출 실패");
            throw new RuntimeException("서버 통신 오류 잠시 후 다시 시도해주세요.");
        }

        return response.getBody();
    }

    /**
     * 수기결제(카유생비) REQUEST SETTING
     * @param transactionId
     * @param clientRequest
     * @param van
     * @return
     */
    private KsnetOldKeyInRequestDTO setKsnetOldKeyInRequest(String transactionId, ClientKeyInRequestDTO clientRequest, Van van) {
        // 할부 개월 셋팅
        String installment = "0";
        if(!clientRequest.getInstallment().isEmpty()){
            installment = clientRequest.getInstallment();
        }

        return new KsnetOldKeyInRequestDTO().builder()
                .mid(van.getVanId())
                .orderNumb(transactionId)
                .userName(clientRequest.getOrderName())
                .productType("REAL")
                .productName(clientRequest.getProductName())
                .totalAmount(String.valueOf(clientRequest.getAmount()))
                .taxFreeAmount("0")
                .interestType("PG")
                .cardNumb(clientRequest.getCardNumber())
                .expiryDate(clientRequest.getExpireDate())
                .installMonth(installment)
                .currencyType("KRW")
                .password2(clientRequest.getPassword())
                .userInfo(clientRequest.getUserInfo())
                .build();
    }

    /**
     * 수기결제 비인증(카드번호, 유효기간) REQUEST SETTING
     * @param transactionId
     * @param clientRequest
     * @param van
     * @return
     */
    private KsnetNonKeyInRequestDTO setKsnetNonKeyInRequest(String transactionId, ClientKeyInRequestDTO clientRequest, Van van){
        // 할부 개월 셋팅
        String installment = "0";
        if(!clientRequest.getInstallment().isEmpty()){
            installment = clientRequest.getInstallment();
        }

        return new KsnetNonKeyInRequestDTO().builder()
                .mid(van.getVanId())
                .orderNumb(transactionId)
                .userName(clientRequest.getOrderName())
                .productType("REAL")
                .productName(clientRequest.getProductName())
                .totalAmount(String.valueOf(clientRequest.getAmount()))
                .taxFreeAmount("0")
                .interestType("PG")
                .cardNumb(clientRequest.getCardNumber())
                .expiryDate(clientRequest.getExpireDate())
                .installMonth(installment)
                .currencyType("KRW")
                .build();
    }

    /**
     * CLIENT REQUEST 내역 SAVE SETTING
     * @param clientRequestDTO
     * @param van
     * @return
     */
    private ClientRequest setClientRequest(ClientKeyInRequestDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .orderId(clientRequestDTO.getOrderId())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .amount(clientRequestDTO.getAmount())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .van(van.getVan())
                .vanId(van.getVanId())
                .build();
    }

    /**
     * 거래승인내역 SAVE SETTING
     * @param transactionId
     * @param method
     * @param clientRequestDTO
     * @param ksnetResponse
     * @param van
     * @param merchant
     * @return
     */
    private Pay setKeyInPay(String transactionId, String method, ClientKeyInRequestDTO clientRequestDTO, KsnetResponse ksnetResponse, Van van, Optional<Merchant> merchant) {
        return new Pay().builder()
                .transactionId(transactionId)
                .method(method)
                .orderId(clientRequestDTO.getOrderId())
                .merchantId(merchant.get().getMerchantId())
                .amount(clientRequestDTO.getAmount())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .installment(clientRequestDTO.getInstallment())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .issuerCardType(ksnetResponse.getData().getIssuerCardType())
                .issuerCardName(ksnetResponse.getData().getIssuerCardName())
                .purchaseCardType(ksnetResponse.getData().getPurchaseCardType())
                .purchaseCardName(ksnetResponse.getData().getPurchaseCardName())
                .cardType(ksnetResponse.getData().getCardType())
                .approvalNumber(ksnetResponse.getData().getApprovalNumb())
                .resultCode(RESULT_CODE)
                .resultMessage(RESULT_MESSAGE)
                .van(van.getVan())
                .vanId(van.getVanId())
                .vanResultCode(ksnetResponse.getData().getRespCode())
                .vanResultMessage(ksnetResponse.getData().getRespMessage())
                .build();
    }


    /**
     * 비밀번호 앞 2자리 검증
     * @param password
     * @return
     */
    private boolean validationPassword(String password) {
        String regexp = "\\d{2}";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }

    /**
     * 생년월일 6자리 검증
     * @param userInfo
     * @return
     */
    private boolean validationUserInfo(String userInfo) {
        String regexp = "^(\\d{2})(\\d{2})(\\d{2})$";

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(userInfo);

        return matcher.matches();
    }

    /**
     * 현재 날짜 기준으로 YYMM 비교 메소드
     * @param expireDate
     * @return
     */
    private boolean validationExpireDate(String expireDate) {
        String yyMM = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));

        if(Integer.parseInt(expireDate) < Integer.parseInt(yyMM)){
            return false;
        }
        return true;
    }
}
