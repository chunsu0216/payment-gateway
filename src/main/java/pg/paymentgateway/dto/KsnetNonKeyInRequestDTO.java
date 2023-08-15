package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class KsnetNonKeyInRequestDTO {

    private String mid; // VAN ID
    private String orderNumb; // 주문번호
    private String userName; //주문자명
    private String productType; //상품구분
    private String productName; //상품명
    private String totalAmount; //총금액
    private String taxFreeAmount; //면세금액
    private String interestType; //이자구분
    private String cardNumb; //카드번호
    private String expiryDate; // 유효기간
    private String installMonth; //할부개월수
    private String currencyType; //통화타입

    @Builder
    public KsnetNonKeyInRequestDTO(String mid, String orderNumb, String userName, String productType, String productName, String totalAmount, String taxFreeAmount, String interestType, String cardNumb, String expiryDate, String installMonth, String currencyType) {
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
    }

    public KsnetNonKeyInRequestDTO() {
    }
}
