package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ErrorResult {

    private String errorCode;
    private String errorMessage;

    @Builder
    public ErrorResult(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResult() {

    }
}
