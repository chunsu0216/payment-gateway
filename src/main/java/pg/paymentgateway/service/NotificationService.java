package pg.paymentgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pg.paymentgateway.entity.Notification;
import pg.paymentgateway.entity.Pay;
import pg.paymentgateway.repository.NotificationRepository;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(Map<String, Object> resultMap, Pay pay) {
        notificationRepository.save(setNotification(resultMap, pay));
    }
    public Notification setNotification(Map<String, Object> resultMap, Pay pay){
        return new Notification().builder()
                .transactionId(pay.getTransactionId())
                .orderId(pay.getOrderId())
                .orderName(pay.getOrderName())
                .merchantId(pay.getMerchant().getMerchantId())
                .amount(pay.getAmount())
                .cardNumber((String) resultMap.get("cardNumber"))
                .issuerCardType((String) resultMap.get("issuerCardType"))
                .issuerCardName((String) resultMap.get("issuerCardName"))
                .purchaseCardType((String) resultMap.get("purchaseCardType"))
                .purchaseCardName((String) resultMap.get("purchaseCardName"))
                .approvalNumber((String) resultMap.get("approvalNumber"))
                .expiryDate((String) resultMap.get("expiryDate"))
                .installMonth((String) resultMap.get("installMonth"))
                .cardType((String) resultMap.get("cardType"))
                .tradeDateTime((String) resultMap.get("tradeDateTime"))
                .retryCount(0)
                .build();
    }
}
