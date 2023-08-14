package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class KsnetOldKeyInRequestDTO {
    private String mid;
    private String orderNumb;
    private String userName;
    private String productType;
    private String productName;
    private String totalAmount;
    private String taxFreeAmount;
    private String interestType;
    private String cardNumb;
    private String expiryDate;
    private String installMonth;
    private String currencyType;
    private String password2;
    private String userInfo;

    @Builder
    public KsnetOldKeyInRequestDTO(String mid, String orderNumb, String userName, String productType, String productName, String totalAmount, String taxFreeAmount, String interestType, String cardNumb, String expiryDate, String installMonth, String currencyType, String password2, String userInfo) {
        this.mid = mid;
        this.orderNumb = orderNumb;
        this.userName = userName;
        this.productType = productType;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.taxFreeAmount = taxFreeAmount;
        this.interestType = interestType;
        this.cardNumb = cardNumb;
        this.expiryDate = expiryDate;
        this.installMonth = installMonth;
        this.currencyType = currencyType;
        this.password2 = password2;
        this.userInfo = userInfo;
    }


    public KsnetOldKeyInRequestDTO() {
    }
}
