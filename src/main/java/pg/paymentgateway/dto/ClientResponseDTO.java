package pg.paymentgateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseDTO {

    private String transactionId;
    private String orderId;
    private String orderName;
    private String resultCode;
    private String resultMessage;
    private String billingToken;

    public ClientResponseDTO() {
    }
}
