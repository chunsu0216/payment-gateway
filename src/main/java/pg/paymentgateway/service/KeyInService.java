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
import pg.paymentgateway.dto.ClientOldKeyInRequestDTO;
import pg.paymentgateway.dto.ClientResponseDTO;
import pg.paymentgateway.dto.KsnetOldKeyInRequestDTO;
import pg.paymentgateway.dto.KsnetResponse;
import pg.paymentgateway.entity.ClientRequest;
import pg.paymentgateway.entity.Merchant;
import pg.paymentgateway.entity.Pay;
import pg.paymentgateway.entity.Van;
import pg.paymentgateway.exception.ForbiddenException;
import pg.paymentgateway.repository.ClientRequestRepository;
import pg.paymentgateway.repository.MerchantRepository;
import pg.paymentgateway.repository.PayRepository;
import pg.paymentgateway.repository.VanRepository;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyInService {

    private final MerchantRepository merchantRepository;
    private final VanRepository vanRepository;
    private final ClientRequestRepository clientRequestRepository;
    private final PayRepository payRepository;
    private final ObjectMapper objectMapper;

    private static final String INVALID_EXPIRE_DATE = "올바르지않은 유효기간입니다.";
    private static final String FORBIDDEN_MERCHANT = "존재하지않은 가맹점 ID입니다.";
    private static final String DUPLICATION_ORDER_ID = "중복된 주문번호입니다.";
    private static final String OLD_KEYIN_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/pay/oldcert";
    private static final String RESULT_CODE = "0000";
    private static final String RESULT_MESSAGE = "정상 승인되었습니다.";

    @Transactional
    public ClientResponseDTO oldCertification(ClientOldKeyInRequestDTO clientRequestDTO) {

        // 유효기간 검증
        if(!this.validationExpireDate(clientRequestDTO.getExpireDate())){
            throw new IllegalArgumentException(INVALID_EXPIRE_DATE);
        }

        // TRANSACTION ID 생성
        String transactionId = UUID.randomUUID().toString();

        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantId(clientRequestDTO.getMerchantId()));
        if(merchant.isEmpty()){
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }else{
            // 주문 ID 중복 검증
            Optional<ClientRequest> orderId = Optional.ofNullable(clientRequestRepository.findClientRequestByOrderId(clientRequestDTO.getOrderId()));

            if(orderId.isPresent()){
                throw new IllegalArgumentException(DUPLICATION_ORDER_ID);
            }

            Van van = vanRepository.findVanByVanId(merchant.get().getVanId());

            //CLIENT REQUEST INSERT
            clientRequestRepository.save(setClientRequest(clientRequestDTO, van));

            // KSNET API CALL
            KsnetResponse ksnetResponse = callKsnetAPI(transactionId, clientRequestDTO, van, "old-keyIn");

            // 정상 응답이 아닐 경우(HTTP STATUS CODE -> 200인데 KSNET 응답 코드가 0000이 아닐 경우)
            if(!"0000".equals(ksnetResponse.getData().getRespCode())){
                throw new IllegalArgumentException(ksnetResponse.getData().getRespMessage());
            }else if("0000".equals(ksnetResponse.getData().getRespCode())){
                // PAY INSERT
                payRepository.save(setOldKeyInPay(transactionId, "old-keyIn", clientRequestDTO, ksnetResponse, van, merchant));
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

    private Pay setOldKeyInPay(String transactionId, String method, ClientOldKeyInRequestDTO clientRequestDTO, KsnetResponse ksnetResponse, Van van, Optional<Merchant> merchant) {
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

    private KsnetResponse callKsnetAPI(String transactionId, ClientOldKeyInRequestDTO clientRequest, Van van, String method){
        KsnetResponse ksnetResponse = null;
        // 구인증 API
        if("old-keyIn".equals(method)){
            KsnetOldKeyInRequestDTO KsnetRequest = this.setKsnetOldKeyInRequest(transactionId, clientRequest, van);
            ksnetResponse = callOldKeyIn(KsnetRequest);
        }

        return ksnetResponse;
    }

    private KsnetResponse callOldKeyIn(KsnetOldKeyInRequestDTO requestDTO){
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
        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(OLD_KEYIN_URL, entity, KsnetResponse.class);

        log.info("response ::::: {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 구인증 API 호출 실패");
            throw new RuntimeException("서버 통신 오류 잠시 후 다시 시도해주세요.");
        }

        return response.getBody();
    }


    private KsnetOldKeyInRequestDTO setKsnetOldKeyInRequest(String transactionId, ClientOldKeyInRequestDTO clientRequest, Van van) {
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

    private ClientRequest setClientRequest(ClientOldKeyInRequestDTO clientRequestDTO, Van van) {
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
