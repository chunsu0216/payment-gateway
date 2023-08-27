package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ClientResponseDTO {

    private String transactionId;
    private String orderId;
    private String orderName;
    private String resultCode;
    private String resultMessage;
    private String billingToken;

    @Builder
    public ClientResponseDTO(String transactionId, String orderId, String orderName, String resultCode, String resultMessage, String billingToken) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.orderName = orderName;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.billingToken = billingToken;
    }

    public ClientResponseDTO() {
    }
}
