package pg.paymentgateway.service.van.ksnet;

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
import pg.paymentgateway.entity.Pay;
import pg.paymentgateway.entity.Van;
import pg.paymentgateway.service.van.VanService;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Ksnet implements VanService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OLD_KEYIN_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/pay/oldcert";
    private static final String NON_KEYIN_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/pay/noncert";
    private static final String CANCEL_URL = "https://paydev.ksnet.co.kr/kspay/webfep/api/v1/card/cancel";

    public Ksnet(RestTemplate restTemplate, ObjectMapper objectMapper){
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    public Map<String, Object> approveKeyIn(String transactionId, ClientKeyInRequestDTO clientRequest, Van van, String method) {
        Map<String, Object> resultMap = new HashMap<>();
        KsnetResponse ksnetResponse = callKsnetAPI(transactionId, clientRequest, van, method);

        if (!"A0200".equals(ksnetResponse.getCode())) {
            // API 응답 정상이 아닐 경우
            resultMap.put("resultCode", "0500");
            resultMap.put("resultMessage", ksnetResponse.getMessage());
        }else{
            String resultCode = ksnetResponse.getData().getRespCode();
            String resultMessage = ksnetResponse.getData().getRespMessage();
            if ("0000".equals(resultCode)) {
                resultMap.put("issuerCardType", ksnetResponse.getData().getIssuerCardType());
                resultMap.put("issuerCardName", ksnetResponse.getData().getIssuerCardName());
                resultMap.put("purchaseCardType", ksnetResponse.getData().getPurchaseCardType());
                resultMap.put("purchaseCardName", ksnetResponse.getData().getPurchaseCardName());
                resultMap.put("cardType", ksnetResponse.getData().getCardType());
                resultMap.put("approvalNumber", ksnetResponse.getData().getApprovalNumb());
                resultMap.put("tradeDateTime", ksnetResponse.getData().getTradeDateTime());
                resultMap.put("installMonth", ksnetResponse.getData().getInstallMonth());
                resultMap.put("expiryDate", ksnetResponse.getData().getExpiryDate());
                resultMap.put("vanTrxId", ksnetResponse.getData().getTid());
            }
            resultMap.put("resultCode", resultCode);
            resultMap.put("resultMessage", resultMessage);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> cancel(Pay pay, String cancelType, ClientKeyInCancelDTO clientRequestDTO, String method) {
        KsnetResponse ksnetResponse = callCancelAPI(setCancelDTO(pay, cancelType, clientRequestDTO), method);
        Map<String, Object> resultMap = new HashMap<>();
        String resultCode = ksnetResponse.getData().getRespCode();
        String resultMessage = ksnetResponse.getData().getRespMessage();

        if ("0000".equals(resultCode)) {
            resultMap.put("vanTrxId", ksnetResponse.getData().getTid());
        }

        resultMap.put("resultCode", resultCode);
        resultMap.put("resultMessage", resultMessage);

        return resultMap;
    }



    private KsnetCancelRequestDTO setCancelDTO(Pay pay, String cancelType, ClientKeyInCancelDTO clientRequestDTO){

        if (cancelType.equals("PARTIAL")) {
            int cancelCount = pay.getApproveCancels().size();
            if(cancelCount == 0){
                cancelCount = 1;
            }else{
                cancelCount = cancelCount + 1;
            }

            return new KsnetCancelRequestDTO().builder()
                    .mid(pay.getVanId())
                    .cancelType(cancelType)
                    .orgTradeKeyType("TID")
                    .orgTradeKey(pay.getVanTrxId())
                    .cancelTotalAmount(String.valueOf(clientRequestDTO.getAmount()))
                    .cancelTaxFreeAmount("0")
                    .cancelSeq(String.valueOf(cancelCount))
                    .build();
        }
        return new KsnetCancelRequestDTO().builder()
                .mid(pay.getVanId())
                .cancelType(cancelType)
                .orgTradeKeyType("TID")
                .orgTradeKey(pay.getVanTrxId())
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

    private KsnetResponse callCancelAPI(KsnetCancelRequestDTO requestDTO, String method){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        headers.setContentType(mediaType);

        if(method.equals("wallet")){
            headers.set("Authorization", "pgapi Mjk5OTE5OTk5OTpNQTAxOkE0RTc2QkRBMzM3RENDQTk1Mjk4RkI0OTVBODREMzY5");
        }else{
            headers.set("Authorization", "pgapi Mjk5OTE5OTk5MDpNQTAxOjNEMUVBOEVBRUM0NzA1MTFBMkIyNUVFMzQwRkI5ODQ4");
        }
        HttpEntity<String> entity = null;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestDTO), headers);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("request => {}", entity.getBody());

        ResponseEntity<KsnetResponse> response = restTemplate.postForEntity(CANCEL_URL, entity, KsnetResponse.class);

        log.info("response => {}", response.toString());

        if(!response.getStatusCode().is2xxSuccessful()){
            log.info("KSNET 승인 취소 API 호출 실패");
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


}
