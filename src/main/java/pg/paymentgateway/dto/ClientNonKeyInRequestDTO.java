package pg.paymentgateway.dto;

import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class ClientNonKeyInRequestDTO {

    @NotNull(message = "가맹점 ID는 필수 값입니다.")
    @NotEmpty(message = "가맹점 ID는 필수 값입니다.")
    private String merchantId;

    @NotNull(message = "주문 ID는 필수 값입니다.")
    @NotEmpty(message = "주문 ID는 필수 값입니다.")
    private String orderId;

    @NotNull(message = "주문자명은 필수 값입니다.")
    @NotEmpty(message = "주문자명은 필수 값입니다.")
    private String orderName;

    @NotNull(message = "상품명은 필수 값입니다.")
    @NotEmpty(message = "상품명은 필수 값입니다.")
    private String productName;

    @NotNull(message = "금액은 필수 값입니다.")
    @PositiveOrZero(message = "금액은 양수 값만 가능합니다.")
    @Min(value = 100, message = "금액은 100원 이상만 가능합니다.")
    private Long amount;

    @NotNull(message = "카드번호는 필수 값입니다.")
    @NotEmpty(message = "카드번호는 필수 값입니다.")
    @CreditCardNumber
    private String cardNumber;

    @NotNull(message = "유효기간은 필수 값입니다.")
    @NotEmpty(message = "유효기간은 필수 값입니다.")
    private String expireDate;

    private String installment;
}
