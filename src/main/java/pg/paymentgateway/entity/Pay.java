package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Getter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PG_PAY")
@Getter
public class Pay extends BaseEntity{
    @Id @GeneratedValue
    private Long idx;
    private String transactionId;
    private String method;
    private String status;
    private String orderId;
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
    private String vanTrxId;
    private String vanResultCode;
    private String vanResultMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_IDX")
    private Merchant merchant;

    @OneToMany(mappedBy = "pay")
    private List<ApproveCancel> approveCancels = new ArrayList<>();

    @Builder
    public Pay(Long idx, String transactionId, String method, String status, Merchant merchant, String orderId, Long amount, String orderName, String productName, String cardNumber, String expireDate, String installment, String password, String userInfo, String issuerCardType, String issuerCardName, String purchaseCardType, String purchaseCardName, String cardType, String approvalNumber, String resultCode, String resultMessage, String van, String vanId, String vanTrxId, String vanResultCode, String vanResultMessage) {
        this.idx = idx;
        this.transactionId = transactionId;
        this.method = method;
        this.status = status;
        this.merchant = merchant;
        this.orderId = orderId;
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
        this.vanTrxId = vanTrxId;
        this.vanResultCode = vanResultCode;
        this.vanResultMessage = vanResultMessage;
    }

    public void updateStatus(String status){
        this.status = status;
    }

    public Pay() {
    }
}
