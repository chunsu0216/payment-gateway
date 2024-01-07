package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "notification")
@Getter
public class Notification {
    @Id
    @GeneratedValue
    private Long idx;
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
    private Integer retryCount;

    public Notification() {

    }
    @Builder
    public Notification(String transactionId, String orderId, String orderName, String merchantId, Long amount, String issuerCardType, String issuerCardName, String purchaseCardType, String purchaseCardName, String approvalNumber, String cardNumber, String expiryDate, String installMonth, String cardType, String tradeDateTime, int retryCount) {
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
        this.retryCount = retryCount;
    }

    public void updateRetryCount(){
        this.retryCount = retryCount + 1;
    }
}
