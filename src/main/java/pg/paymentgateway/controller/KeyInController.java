package pg.paymentgateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pg.paymentgateway.dto.ClientKeyInCancelDTO;
import pg.paymentgateway.dto.ClientKeyInRequestDTO;
import pg.paymentgateway.service.KeyInService;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "카드 수기결제 API", description = "카드번호/유효기간/생년월일/비밀번호 등을 이용한 카드 승인 요청 API입니다. 실제 PG사와 연동되어있으나 테스트서버 연동임으로 실제 결제 처리되지않습니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@Schema(hidden = true)
public class KeyInController {

    private final KeyInService keyInService;

    @Operation(summary = "구인증 결제", description = "REQUEST HEADER에 Authorizaion : c674ccc0-2f95-11ee-bf23-f53b19ae9a8d 및 merchantId : merchant01 셋팅하셔야합니다. \n 카드번호/유효기간/생년월일/비밀번호를 이용한 카드 승인 요청 API 입니다.")
    @Parameters({
            @Parameter(name = "merchantId", description = "가맹점 ID", example = "merchant01"),
            @Parameter(name = "orderId", description = "가맹점이 부여한 거래 ID", example = "order_20230903121212"),
            @Parameter(name = "orderName", description = "주문자명", example = "홍길동"),
            @Parameter(name = "productName", description = "상품명", example = "테스트 상품"),
            @Parameter(name = "amount", description = "금액", example = "1000"),
            @Parameter(name = "cardNumber", description = "카드번호(-)제외", example = "1234123412341234"),
            @Parameter(name = "expireDate", description = "유효기간(YY/MM)", example = "2708"),
            @Parameter(name = "installment", description = "할부개월수", example = "0"),
            @Parameter(name = "password", description = "비밀번호 앞 2자리", example = "00"),
            @Parameter(name = "userInfo", description = "생년월일(6자리)", example = "940216")
    })
    @PostMapping("/api/v1/card/old-certification")
    public Object keyIn(@RequestBody @Validated ClientKeyInRequestDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        return keyInService.keyIn(clientRequestDTO, "old-keyIn", request);
    }
    @Operation(summary = "비인증 결제", description = "REQUEST HEADER에 Authorizaion : c674ccc0-2f95-11ee-bf23-f53b19ae9a8d 및 merchantId : merchant01 셋팅하셔야합니다. \n 카드번호/유효기간을 이용한 카드 승인 요청 API 입니다.")
    @Parameters({
            @Parameter(name = "merchantId", description = "가맹점 ID", example = "merchant01"),
            @Parameter(name = "orderId", description = "가맹점이 부여한 거래 ID", example = "order_20230903121212"),
            @Parameter(name = "orderName", description = "주문자명", example = "홍길동"),
            @Parameter(name = "productName", description = "상품명", example = "테스트 상품"),
            @Parameter(name = "amount", description = "금액", example = "1000"),
            @Parameter(name = "cardNumber", description = "카드번호(-)제외", example = "1234123412341234"),
            @Parameter(name = "expireDate", description = "유효기간(YY/MM)", example = "2708"),
            @Parameter(name = "installment", description = "할부개월수", example = "0")

    })
    @PostMapping("/api/v1/card/non-certification")
    public Object nonKeyIn(@RequestBody @Validated ClientKeyInRequestDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        return keyInService.keyIn(clientRequestDTO, "non-keyIn", request);
    }
    @Operation(summary = "결제 취소", description = "REQUEST HEADER에 Authorizaion : c674ccc0-2f95-11ee-bf23-f53b19ae9a8d 및 merchantId : merchant01 셋팅하셔야합니다. \n 카드 승인 후 결제 취소 요청 API입니다.")
    @Parameters({
            @Parameter(name = "merchantId", description = "가맹점 ID", example = "merchant01"),
            @Parameter(name = "orderNumber", description = "가맹점이 부여한 원거래 ID", example = "order_20230903121212"),
            @Parameter(name = "transactionId", description = "원거래 ID", example = "테스트"),
            @Parameter(name = "amount", description = "금액", example = "1000")
    })
    @PostMapping("/api/v1/card/cancel")
    public Object cancel(@RequestBody @Validated ClientKeyInCancelDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request){

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        return keyInService.cancel(clientRequestDTO, request);
    }
}
