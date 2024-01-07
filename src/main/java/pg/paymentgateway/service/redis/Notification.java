package pg.paymentgateway.service.redis;

import lombok.Builder;
import lombok.Data;

@Data
public class Notification {
    private String transactionId;
    private String orderId;
    private String orderName;
    private String merchantId;
    private Long amount;
    private String issuerCardType;
    private String issuerCardName;
    private String purchaseCardType;
    private String purchaseCardName;
    private String approvalNumber;
    private String cardNumber;
    private String expiryDate;
    private String installMonth;
    private String cardType;
    private String tradeDateTime;

    public Notification() {

    }
    @Builder
    public Notification(String transactionId, String orderId, String orderName, String merchantId, Long amount, String issuerCardType, String issuerCardName, String purchaseCardType, String purchaseCardName, String approvalNumber, String cardNumber, String expiryDate, String installMonth, String cardType, String tradeDateTime) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.merchantId = merchantId;
        this.amount = amount;
        this.issuerCardType = issuerCardType;
        this.issuerCardName = issuerCardName;
        this.purchaseCardType = purchaseCardType;
        this.purchaseCardName = purchaseCardName;
        this.approvalNumber = approvalNumber;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.installMonth = installMonth;
        this.cardType = cardType;
        this.tradeDateTime = tradeDateTime;
    }
}
