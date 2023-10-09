package pg.paymentgateway.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class ClientKeyInCancelDTO {

    @NotNull(message = "가맹점 ID는 필수 값입니다.")
    @NotEmpty(message = "가맹점 ID는 필수 값입니다.")
    private String merchantId;

    private String orderId;
    private String transactionId;

    @NotNull(message = "금액은 필수 값입니다.")
    @PositiveOrZero(message = "금액은 양수 값만 가능합니다.")
    @Min(value = 100, message = "금액은 100원 이상만 가능합니다.")
    private Long amount;
}
