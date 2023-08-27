package pg.paymentgateway.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "PG_APPROVE_CANCEL")
@Getter
public class ApproveCancel {

    @Id @GeneratedValue
    private Long idx;
    private String cancelTransactionId;
    private String merchantId;
    private String status;
    private Long amount;
    private String rootOrderId;
    private String van;
    private String vanId;
    private String vanTrxId;
    private String vanResultCode;
    private String vanResultMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_TRANSACTION_IDX")
    private Pay pay;

    public ApproveCancel() {
    }

    @Builder
    public ApproveCancel(Long idx, String cancelTransactionId, String merchantId, String status, Long amount, String rootOrderId, String van, String vanId, String vanTrxId, String vanResultCode, String vanResultMessage, Pay pay) {
        this.idx = idx;
        this.cancelTransactionId = cancelTransactionId;
        this.merchantId = merchantId;
        this.status = status;
        this.amount = amount;
        this.rootOrderId = rootOrderId;
        this.van = van;
        this.vanId = vanId;
        this.vanTrxId = vanTrxId;
        this.vanResultCode = vanResultCode;
        this.vanResultMessage = vanResultMessage;
        this.pay = pay;
    }
}
