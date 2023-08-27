package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class BillingToken extends BaseEntity{
    @Id @GeneratedValue
    private Long idx;
    private String vanTrxId;
    private String password;
    private String issuerCardType;
    private String issuerCardName;
    private String purchaseCardType;
    private String purcharseCardName;
    private String cardType;
    private String cardNumber;
    private String billingToken;

    public BillingToken() {
    }

    @Builder
    public BillingToken(Long idx, String vanTrxId, String password, String issuerCardType, String issuerCardName, String purchaseCardType, String purcharseCardName, String cardType, String cardNumber, String billingToken) {
        this.idx = idx;
        this.vanTrxId = vanTrxId;
        this.password = password;
        this.issuerCardType = issuerCardType;
        this.issuerCardName = issuerCardName;
        this.purchaseCardType = purchaseCardType;
        this.purcharseCardName = purcharseCardName;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.billingToken = billingToken;
    }
}
