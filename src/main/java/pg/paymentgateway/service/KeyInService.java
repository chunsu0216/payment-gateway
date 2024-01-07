package pg.paymentgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pg.paymentgateway.dto.*;
import pg.paymentgateway.entity.*;
import pg.paymentgateway.exception.ForbiddenException;
import pg.paymentgateway.repository.*;
import pg.paymentgateway.entity.Notification;
import pg.paymentgateway.service.redis.RedisPublisher;
import pg.paymentgateway.service.van.VanService;
import pg.paymentgateway.service.webhook.WebHookService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
    private final ApproveCancelRepository approveCancelRepository;
    private final RequestSaveService requestSaveService;
    private final Map<String, VanService> vanServiceMap;
    private final WebHookService webHookService;
    private final NotificationService notificationService;

    private static final String INVALID_EXPIRE_DATE = "올바르지않은 유효기간입니다.";
    private static final String INVALID_BIRTHDAY = "올바르지않은 생년월일입니다.";
    private static final String INVALID_PASSWORD = "올바르지않은 비밀번호입니다.";
    private static final String FORBIDDEN_MERCHANT = "존재하지않은 가맹점 ID입니다.";
    private static final String INTERNAL_EXCEPTION = "내부 서버 오류입니다. 잠시 후 다시 시도해주세요.";
    private static final String DUPLICATION_ORDER_ID = "중복된 주문번호입니다.";
    private static final String RESULT_CODE = "0000";
    private static final String RESULT_MESSAGE = "정상 승인되었습니다.";
    private static final String RESULT_CANCEL_MESSAGE = "정상 취소되었습니다.";

    @Transactional
    public Object keyIn(ClientKeyInRequestDTO clientRequestDTO, String method, HttpServletRequest request) {

        // 유효기간 검증
        if(!validationExpireDate(clientRequestDTO.getExpireDate())){
            throw new IllegalArgumentException(INVALID_EXPIRE_DATE);
        }

        // 구인증(카유생비) 일 경우 추가 검증
        if ("old-keyIn".equals(method)) {
            // 생년월일 검증
            if(!validationUserInfo(clientRequestDTO.getUserInfo())){
                throw new IllegalArgumentException(INVALID_BIRTHDAY);
            }
            // 비밀번호 자릿수 검증
            if(!validationPassword(clientRequestDTO.getPassword())){
                throw new IllegalArgumentException(INVALID_PASSWORD);
            }
        }

        // TRANSACTION ID 생성
        String transactionId = "T" + UUID.randomUUID();


        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantIdAndPaymentKey(clientRequestDTO.getMerchantId(), request.getHeader("Authorization")));

        if(merchant.isEmpty()){
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }else{
            // 가맹점 별 주문 ID 중복 검증
            Optional<ClientRequest> orderId = Optional.ofNullable(clientRequestRepository.findClientRequestByOrderIdAndMerchantId(clientRequestDTO.getOrderId(), clientRequestDTO.getMerchantId()));

            if(orderId.isPresent()){
                throw new IllegalArgumentException(DUPLICATION_ORDER_ID);
            }

            List<Van> vans = merchant.get().getVans();
            Van van = null;

            for (Van findVan : vans) {
                if(findVan.getMerchant().getMerchantId().equals(merchant.get().getMerchantId()) && findVan.getMethod().equals(method)){
                    van = findVan;
                    break;
                }
            }
            //CLIENT REQUEST INSERT
            requestSaveService.saveKeyInRequest(clientRequestDTO, van);
            // 상위 PG API CALL
            VanService vanService = vanServiceMap.get(van.getVan());
            Map<String, Object> resultMap = vanService.approveKeyIn(transactionId, clientRequestDTO, van, method);
            String resultCode = (String) resultMap.get("resultCode");
            String resultMessage = (String) resultMap.get("resultMessage");
            if("0500".equals(resultCode)){
                // API 응답 정상이 아닐 경우
                return new ErrorResultDTO().builder()
                        .errorCode("0500")
                        .errorMessage(INTERNAL_EXCEPTION)
                        .build();
            }else{
                // 정상 승인 응답이 아닐 경우(HTTP STATUS CODE -> 200인데 KSNET 응답 코드가 0000이 아닐 경우)
                if(!"0000".equals(resultCode)){
                    throw new IllegalArgumentException(resultMessage);
                }else if("0000".equals(resultCode)){
                    // PAY INSERT
                    Pay pay = payRepository.save(setKeyInPay(transactionId, method, clientRequestDTO, resultMap, van, merchant));

                    // 가맹점 거래 결과 노티 데이터 생성(Redis)
                    notificationService.createNotification(resultMap, pay);
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
     * 거래승인내역 SAVE SETTING
     * @param transactionId
     * @param method
     * @param clientRequestDTO
     * @param resultMap
     * @param van
     * @param merchant
     * @return
     */
    private Pay setKeyInPay(String transactionId, String method, ClientKeyInRequestDTO clientRequestDTO, Map<String, Object> resultMap, Van van, Optional<Merchant> merchant) {
        return new Pay().builder()
                .transactionId(transactionId)
                .method(method)
                .status("승인")
                .orderId(clientRequestDTO.getOrderId())
                .merchant(merchant.get())
                .amount(clientRequestDTO.getAmount())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .installment(clientRequestDTO.getInstallment())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .issuerCardType(resultMap.get("issuerCardType").toString())
                .issuerCardName(resultMap.get("issuerCardName").toString())
                .purchaseCardType(resultMap.get("purchaseCardType").toString())
                .purchaseCardName(resultMap.get("purchaseCardName").toString())
                .cardType(resultMap.get("cardType").toString())
                .approvalNumber(resultMap.get("approvalNumber").toString())
                .resultCode(resultMap.get("resultCode").toString())
                .resultMessage(resultMap.get("resultMessage").toString())
                .van(van.getVan())
                .vanId(van.getVanId())
                .vanTrxId(resultMap.get("vanTrxId").toString())
                .vanResultCode(resultMap.get("resultCode").toString())
                .vanResultMessage(resultMap.get("resultMessage").toString())
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

    @Transactional
    public Object cancel(ClientKeyInCancelDTO clientRequestDTO, HttpServletRequest request) {

        // CLIENT REQUSET SAVE
        requestSaveService.saveCancelRequest(clientRequestDTO);

        // 가맹점 ID 검증
        Optional<Merchant> merchant = Optional.ofNullable(merchantRepository.findMerchantByMerchantIdAndPaymentKey(clientRequestDTO.getMerchantId(), request.getHeader("Authorization")));
        if(merchant.isEmpty()){
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }

        // 거래 ID 검증
        if(clientRequestDTO.getOrderId().isEmpty() && clientRequestDTO.getTransactionId().isEmpty()){
            throw new IllegalArgumentException("가맹점 주문번호 ID 또는 승인 후 부여한 transactionId를 셋팅하셔야합니다.");
        }

        Optional<Pay> pay = Optional.ofNullable(payRepository.findPayByTransactionIdOrOrderId(clientRequestDTO.getTransactionId(), clientRequestDTO.getOrderId()));

        if(pay.isEmpty()){
            throw new IllegalArgumentException("원거래 ID 또는 주문번호를 확인할 수 없습니다.");
        }

        if(pay.get().getAmount() < clientRequestDTO.getAmount()){
            new IllegalArgumentException("취소 요청 금액이 원거래 승인 금액보다 큽니다.");
        }

        List<ApproveCancel> approveCancels = pay.get().getApproveCancels();

        long totalCancelAmount = 0;
        long remainAmount = 0;

        for (ApproveCancel approveCancel : approveCancels) {
            totalCancelAmount += approveCancel.getAmount();
        }

        remainAmount = pay.get().getAmount() - totalCancelAmount;

        if(remainAmount == 0){
            throw new IllegalArgumentException("기취소 거래입니다.");
        }

        if(clientRequestDTO.getAmount() > remainAmount){
            throw new IllegalArgumentException("취소 요청 금액이 기취소된 금액보다 큽니다.");
        }

        String cancelType = "";

        // 취소 거래 내역 확인
        if(pay.get().getApproveCancels().size() == 0 && pay.get().getAmount() == clientRequestDTO.getAmount()){
            cancelType = "FULL";
        }else{
            cancelType = "PARTIAL";
        }

        List<Van> vans = merchant.get().getVans();
        String vanId = "";
        Van van = null;
        String method = pay.get().getMethod();

        for (Van findVan : vans) {
            if(findVan.getMerchant().getMerchantId().equals(merchant.get().getMerchantId()) && findVan.getMethod().equals(method)){
                van = findVan;
                break;
            }
        }

        String transactionId = "T" + UUID.randomUUID();
        VanService vanService = vanServiceMap.get(van.getVan());
        Map<String, Object> resultMap = vanService.cancel(pay.get(), cancelType, clientRequestDTO, method);
        String resultCode = (String) resultMap.get("resultCode");
        String resultMessage = (String) resultMap.get("resultMessage");

        if (resultCode.equals("0000")) {
            // 취소 원장 저장
            approveCancelRepository.save(new ApproveCancel().builder()
                    .cancelTransactionId(transactionId)
                    .merchantId(clientRequestDTO.getMerchantId())
                    .status(cancelType)
                    .amount(clientRequestDTO.getAmount())
                    .rootOrderId(pay.get().getOrderId())
                    .van(pay.get().getVan())
                    .vanId(pay.get().getVanId())
                    .vanTrxId(resultMap.get("vanTrxId").toString())
                    .vanResultCode(resultCode)
                    .vanResultMessage(resultMessage)
                    .pay(pay.get())
                    .build()
            );

            if(pay.get().getAmount() == totalCancelAmount + clientRequestDTO.getAmount()){
                pay.get().updateStatus("승인취소");
            }
        }else{
            throw new IllegalArgumentException(resultMessage);
        }
        return new ClientResponseDTO().builder()
                .resultCode(RESULT_CODE)
                .resultMessage(RESULT_CANCEL_MESSAGE)
                .transactionId(transactionId)
                .build();
    }
}
