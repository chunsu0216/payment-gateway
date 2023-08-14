package pg.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ErrorResultDTO {

    private String errorCode;
    private String errorMessage;

    @Builder
    public ErrorResultDTO(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResultDTO() {

    }
}
