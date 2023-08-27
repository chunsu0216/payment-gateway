package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class KsnetBilingRequestDTO {

    private String mid;
    private String cardNumb;
    private String expiryDate;
    private String password2;
    private String userInfo;

    @Builder
    public KsnetBilingRequestDTO(String mid, String cardNumb, String expiryDate, String password2, String userInfo) {
        this.mid = mid;
        this.cardNumb = cardNumb;
        this.expiryDate = expiryDate;
        this.password2 = password2;
        this.userInfo = userInfo;
    }

    public KsnetBilingRequestDTO() {
    }
}
