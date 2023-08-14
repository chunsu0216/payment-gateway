package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pg.paymentgateway.repository.PayRepository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PG_PAY")
@Getter
public class Pay extends BaseEntity{
    @Id @GeneratedValue
    private Long id;
    private String transactionId;
    private String method;
    private String orderId;
    private String merchantId;
    private Long amount;
    private String orderName;
    private String productName;
    private String cardNumber;
    private String expireDate;
    private String installment;
    private String password;
    private String userInfo;
    private String issuerCardType;
    private String issuerCardName;
    private String purchaseCardType;
    private String purchaseCardName;
    private String cardType;
    private String approvalNumber;
    private String resultCode;
    private String resultMessage;
    private String van;
    private String vanId;
    private String vanResultCode;
    private String vanResultMessage;

    @Builder
    public Pay(Long id, String transactionId, String method, String orderId, String merchantId, Long amount, String orderName, String productName, String cardNumber, String expireDate, String installment, String password, String userInfo, String issuerCardType, String issuerCardName, String purchaseCardType, String purchaseCardName, String cardType, String approvalNumber, String resultCode, String resultMessage, String van, String vanId, String vanResultCode, String vanResultMessage) {
        this.id = id;
        this.transactionId = transactionId;
        this.method = method;
        this.orderId = orderId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.orderName = orderName;
        this.productName = productName;
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.installment = installment;
        this.password = password;
        this.userInfo = userInfo;
        this.issuerCardType = issuerCardType;
        this.issuerCardName = issuerCardName;
        this.purchaseCardType = purchaseCardType;
        this.purchaseCardName = purchaseCardName;
        this.cardType = cardType;
        this.approvalNumber = approvalNumber;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.van = van;
        this.vanId = vanId;
        this.vanResultCode = vanResultCode;
        this.vanResultMessage = vanResultMessage;
    }

    public Pay() {
    }
}
