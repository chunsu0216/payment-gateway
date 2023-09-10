package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PG_CLIENT_REQUEST")
@Setter
public class ClientRequest extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String merchantId;
    private String transactionType;
    private String orderId;
    private String orderName;
    private String productName;
    private Long amount;
    private String cardNumber;
    private String installment;
    private String billingToken;
    private String expireDate;
    private String password;
    private String userInfo;
    private String van;
    private String vanId;
    private String rootTransactionId;

    @Builder
    public ClientRequest(String merchantId, String transactionType, String rootTransactionId, String orderId, String orderName, String productName, Long amount, String cardNumber, String expireDate, String password, String userInfo, String van, String vanId, String installment, String billingToken) {
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.transactionType = transactionType;
        this.rootTransactionId = rootTransactionId;
        this.orderName = orderName;
        this.productName = productName;
        this.amount = amount;
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.password = password;
        this.userInfo = userInfo;
        this.van = van;
        this.vanId = vanId;
        this.installment = installment;
        this.billingToken = billingToken;
    }

    public ClientRequest() {
    }
}
