package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class KsnetCancelRequestDTO {

    private String mid;
    private String cancelType;
    private String orgTradeKeyType;
    private String orgTradeKey;
    private String cancelTotalAmount;
    private String cancelTaxFreeAmount;
    private String cancelSeq;

    public KsnetCancelRequestDTO() {
    }

    @Builder
    public KsnetCancelRequestDTO(String mid, String cancelType, String orgTradeKeyType, String orgTradeKey, String cancelTotalAmount, String cancelTaxFreeAmount, String cancelSeq) {
        this.mid = mid;
        this.cancelType = cancelType;
        this.orgTradeKeyType = orgTradeKeyType;
        this.orgTradeKey = orgTradeKey;
        this.cancelTotalAmount = cancelTotalAmount;
        this.cancelTaxFreeAmount = cancelTaxFreeAmount;
        this.cancelSeq = cancelSeq;
    }
}
