package pg.paymentgateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pg.paymentgateway.dto.ErrorResult;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExceptionHandler(IllegalArgumentException e){
        log.error("[IllegalArgumentException handler]", e);

        return new ErrorResult().builder()
                .errorCode("0400")
                .errorMessage(e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResult forbiddenExceptionHandler(ForbiddenException e){
        log.error("[forbiddenExceptionHandler handler]", e);

        return new ErrorResult().builder()
                .errorCode("0403")
                .errorMessage(e.getMessage())
                .build();
    }
}
