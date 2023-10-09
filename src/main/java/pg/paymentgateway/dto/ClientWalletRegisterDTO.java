package pg.paymentgateway.dto;

import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ClientWalletRegisterDTO {
    @NotNull(message = "가맹점 ID는 필수 값입니다.")
    @NotEmpty(message = "가맹점 ID는 필수 값입니다.")
    private String merchantId;

    @NotNull(message = "카드번호는 필수 값입니다.")
    @NotEmpty(message = "카드번호는 필수 값입니다.")
    @CreditCardNumber
    private String cardNumber;

    @NotNull(message = "유효기간은 필수 값입니다.")
    @NotEmpty(message = "유효기간은 필수 값입니다.")
    @Pattern(regexp = "^[0-9]*$", message = "유효기간은 숫자만 입력 가능합니다.")
    @Size(min = 4, max = 4, message = "유효기간은 4자리입니다 YYMM 형식을 맞춰주세요")
    private String expireDate;

    private String password;
    private String userInfo;
}
