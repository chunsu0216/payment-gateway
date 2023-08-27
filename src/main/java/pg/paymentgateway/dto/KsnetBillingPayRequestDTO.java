package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class KsnetBillingPayRequestDTO {

    private String mid;
    private String orderNumb;
    private String userName;
    private String productType;
    private String productName;
    private String totalAmount;
    private String taxFreeAmount;
    private String interestType;
    private String billingToken;
    private String installMonth;
    private String currencyType;

    public KsnetBillingPayRequestDTO() {
    }

    @Builder
    public KsnetBillingPayRequestDTO(String mid, String orderNumb, String userName, String productType, String productName, String totalAmount, String taxFreeAmount, String interestType, String billingToken, String installMonth, String currencyType) {
        this.mid = mid;
        this.orderNumb = orderNumb;
        this.userName = userName;
        this.productType = productType;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.taxFreeAmount = taxFreeAmount;
        this.interestType = interestType;
        this.billingToken = billingToken;
        this.installMonth = installMonth;
        this.currencyType = currencyType;
    }
}
