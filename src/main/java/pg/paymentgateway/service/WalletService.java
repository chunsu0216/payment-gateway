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
import org.springframework.web.client.RestTemplate;
import pg.paymentgateway.dto.*;
import pg.paymentgateway.entity.*;
import pg.paymentgateway.exception.ForbiddenException;
import pg.paymentgateway.repository.BillingTokenRepository;
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
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private static final String INVALID_EXPIRE_DATE = "올바르지않은 유효기간입니다.";
    private static final String INVALID_BIRTHDAY = "올바르지않은 생년월일입니다.";
    private static final String INVALID_PASSWORD = "올바르지않은 비밀번호입니다.";
    private static final String FORBIDDEN_MERCHANT = "존재하지않은 가맹점 ID입니다.";
    private static final String BILLING_REGISTER_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/billing/regist";
    private static final String BILLING_PAY_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/billing/pay";
    private final ObjectMapper objectMapper;
    private final MerchantRepository merchantRepository;
    private final ClientRequestRepository clientRequestRepository;
    private final BillingTokenRepository billingTokenRepository;
    private final PayRepository payRepository;
    private static final String RESULT_CODE = "0000";
    private static final String RESULT_REGISTER_MESSAGE = "정상 등록되었습니다.";
    private static final String RESULT_PAY_MESSAGE = "정상 처리되었습니다.";

    public Object register(ClientWalletRegisterDTO clientRequestDTO){

        // 유효기간 검증
        if(!this.validationExpireDate(clientRequestDTO.getExpireDate())){
            throw new IllegalArgumentException(INVALID_EXPIRE_DATE);
        }

        // 생년월일 검증
        if(!this.validationUserInfo(clientRequestDTO.getUserInfo())){
            throw new IllegalArgumentException(INVALID_BIRTHDAY);
        }

        // 비밀번호 자릿수 검증
        if(!this.validationPassword(clientRequestDTO.getPassword())){
            throw new IllegalArgumentException(INVALID_PASSWORD);
        }

        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantId(clientRequestDTO.getMerchantId()));

        if (merchant.isEmpty()) {
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }

        List<Van> vans = merchant.get().getVans();

        String vanId = "";
        Van van = null;

        for (Van findVan : vans) {
            if(findVan.getMerchant().getMerchantId().equals(merchant.get().getMerchantId()) && findVan.getMethod().equals("wallet")){
                van = findVan;
                break;
            }
        }

        //CLIENT REQUEST INSERT
        clientRequestRepository.save(setClientRequest(clientRequestDTO, van));

        // KSNET API CALL
        KsnetResponse ksnetResponse = null;

        ksnetResponse = callKsnetRegisterAPI(clientRequestDTO, van);

        if (ksnetResponse.getData().getRespCode().equals("0000")) {
            billingTokenRepository.save(new BillingToken().builder()
                    .vanTrxId(ksnetResponse.getData().getTid())
                    .password(clientRequestDTO.getPassword())
                    .issuerCardType(ksnetResponse.getData().getIssuerCardType())
                    .issuerCardName(ksnetResponse.getData().getIssuerCardName())
                    .purchaseCardType(ksnetResponse.getData().getPurchaseCardType())
                    .purcharseCardName(ksnetResponse.getData().getPurchaseCardName())
                    .cardType(ksnetResponse.getData().getCardType())
                    .cardNumber(ksnetResponse.getData().getCardNumb())
                    .billingToken(ksnetResponse.getData().getBillingToken())
                    .build()
            );
        }else{
            throw new IllegalArgumentException(ksnetResponse.getData().getRespMessage());
        }

        return new ClientResponseDTO().builder()
                .resultCode(RESULT_CODE)
                .resultMessage(RESULT_REGISTER_MESSAGE)
                .billingToken(ksnetResponse.getData().getBillingToken())
                .build();
    }



    public Object pay(ClientWalletPayRequestDTO clientRequestDTO) {
        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantId(clientRequestDTO.getMerchantId()));

        if (merchant.isEmpty()) {
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }

        List<Van> vans = merchant.get().getVans();

        String vanId = "";
        Van van = null;

        for (Van findVan : vans) {
            if(findVan.getMerchant().getMerchantId().equals(merchant.get().getMerchantId()) && findVan.getMethod().equals("wallet")){
                van = findVan;
                break;
            }
        }

        //CLIENT REQUEST INSERT
        clientRequestRepository.save(setClientPayRequest(clientRequestDTO, van));

        // KSNET API CALL
        KsnetResponse ksnetResponse = null;

        // TRANSACTION ID 생성
        String transactionId = "T" + UUID.randomUUID().toString();

        ksnetResponse = callKsnetPayAPI(clientRequestDTO, van, transactionId);

        if (ksnetResponse.getData().getRespCode().equals("0000")) {
            // PG_PAY 저장
            payRepository.save(new Pay().builder()
                    .transactionId(transactionId)
                    .method("wallet")
                    .status("승인")
                    .amount(clientRequestDTO.getAmount())
                    .productName(clientRequestDTO.getProductName())
                    .cardNumber(ksnetResponse.getData().getCardNumb())
                    .expireDate(ksnetResponse.getData().getExpiryDate())
                    .installment(ksnetResponse.getData().getInstallMonth())
                    .purchaseCardType(ksnetResponse.getData().getPurchaseCardType())
                    .purchaseCardName(ksnetResponse.getData().getPurchaseCardName())
                    .issuerCardType(ksnetResponse.getData().getIssuerCardType())
                    .issuerCardName(ksnetResponse.getData().getIssuerCardName())
                    .cardType(ksnetResponse.getData().getCardType())
                    .approvalNumber(ksnetResponse.getData().getApprovalNumb())
                    .resultCode("0000")
                    .merchant(merchant.get())
                    .van(van.getVan())
                    .vanId(van.getVanId())
                    .vanTrxId(ksnetResponse.getData().getTid())
                    .vanResultCode(ksnetResponse.getData().getRespCode())
                    .vanResultMessage(ksnetResponse.getData().getRespMessage())
                    .build()
            );
        }else{
            throw new IllegalArgumentException(ksnetResponse.getData().getRespMessage());
        }

        return new ClientResponseDTO().builder()
                .transactionId(transactionId)
                .resultCode(RESULT_CODE)
                .resultMessage(RESULT_PAY_MESSAGE)
                .build();
    }

    private KsnetResponse callKsnetPayAPI(ClientWalletPayRequestDTO clientRequestDTO, Van van, String transactionId) {
        return callPayAPI(new KsnetBillingPayRequestDTO().builder()
                .mid(van.getVanId())
                .orderNumb(transactionId)
                .userName(clientRequestDTO.getOrderName())
                .productType("REAL")
                .productName(clientRequestDTO.getProductName())
                .totalAmount(String.valueOf(clientRequestDTO.getAmount()))
                .taxFreeAmount("0")
                .interestType("PG")
                .billingToken(clientRequestDTO.getBillingToken())
                .installMonth(clientRequestDTO.getInstallment())
                .currencyType("KRW")
                .build());
    }

    private KsnetResponse callPayAPI(KsnetBillingPayRequestDTO requestDTO) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);
        headers.set("Authorization", "pgapi Mjk5OTE5OTk5OTpNQTAxOkE0RTc2QkRBMzM3RENDQTk1Mjk4RkI0OTVBODREMzY5");
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestDTO), headers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("request => {}", entity.getBody());

        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(BILLING_PAY_URL, entity, KsnetResponse.class);

        log.info("response => {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 빌링결제 API 호출 실패");
            throw new RuntimeException("서버 통신 오류 잠시 후 다시 시도해주세요.");
        }

        return response.getBody();
    }

    private ClientRequest setClientPayRequest(ClientWalletPayRequestDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .amount(clientRequestDTO.getAmount())
                .installment(clientRequestDTO.getInstallment())
                .billingToken(clientRequestDTO.getBillingToken())
                .van(van.getVan())
                .vanId(van.getVanId())
                .build();
    }

    private KsnetResponse callKsnetRegisterAPI(ClientWalletRegisterDTO clientRequestDTO, Van van) {
        return callRegisterAPI(new KsnetBilingRequestDTO().builder()
                .mid(van.getVanId())
                .cardNumb(clientRequestDTO.getCardNumber())
                .expiryDate(clientRequestDTO.getExpireDate())
                .password2(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .build());
    }

    private KsnetResponse callRegisterAPI(KsnetBilingRequestDTO requestDTO) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);
        headers.set("Authorization", "pgapi Mjk5OTE5OTk5OTpNQTAxOkE0RTc2QkRBMzM3RENDQTk1Mjk4RkI0OTVBODREMzY5");
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestDTO), headers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("request => {}", entity.getBody());

        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(BILLING_REGISTER_URL, entity, KsnetResponse.class);

        log.info("response => {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 카드등록 API 호출 실패");
            throw new RuntimeException("서버 통신 오류 잠시 후 다시 시도해주세요.");
        }

        return response.getBody();
    }

    /**
     * CLIENT REQUEST 내역 SAVE SETTING
     * @param clientRequestDTO
     * @param van
     * @return
     */
    private ClientRequest setClientRequest(ClientWalletRegisterDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .van(van.getVan())
                .vanId(van.getVanId())
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
