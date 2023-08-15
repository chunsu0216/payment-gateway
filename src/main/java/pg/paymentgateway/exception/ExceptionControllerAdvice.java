package pg.paymentgateway.exception;

import com.sun.jdi.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pg.paymentgateway.dto.ErrorResultDTO;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResultDTO illegalExceptionHandler(IllegalArgumentException e){
        log.error("[IllegalArgumentException handler]", e);

        return new ErrorResultDTO().builder()
                .errorCode("0400")
                .errorMessage(e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResultDTO forbiddenExceptionHandler(ForbiddenException e){
        log.error("[forbiddenExceptionHandler handler]", e);

        return new ErrorResultDTO().builder()
                .errorCode("0403")
                .errorMessage(e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResultDTO internalServerException(RuntimeException e){
        log.error("[internalServerException handler]", e);

        return new ErrorResultDTO().builder()
                .errorCode("0500")
                .errorMessage(e.getMessage())
                .build();
    }
}
