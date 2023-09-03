package pg.paymentgateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pg.paymentgateway.dto.ClientWalletPayRequestDTO;
import pg.paymentgateway.dto.ClientWalletRegisterDTO;
import pg.paymentgateway.service.WalletService;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "간편결제 API", description = "카드 정보를 등록 후 토큰값을 이용한 간편 결제 API 입니다.")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "카드 등록 API", description = "REQUEST HEADER에 Authorizaion : c674ccc0-2f95-11ee-bf23-f53b19ae9a8d 셋팅하셔야합니다. \n 간편결제를 위한 카드 등록 요청 API 입니다.")
    @Parameters({
            @Parameter(name = "merchantId", description = "가맹점 ID", example = "merchant01"),
            @Parameter(name = "cardNumber", description = "카드번호(-)제외", example = "1234123412341234"),
            @Parameter(name = "expireDate", description = "유효기간(YY/MM)", example = "2708"),
            @Parameter(name = "installment", description = "할부개월수", example = "0"),
            @Parameter(name = "password", description = "비밀번호 앞 2자리", example = "00"),
            @Parameter(name = "userInfo", description = "생년월일(6자리)", example = "940216")
    })
    @PostMapping("/api/v1/wallet/register")
    public Object register(@RequestBody @Validated ClientWalletRegisterDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request){
        return walletService.register(clientRequestDTO, request);
    }

    @Operation(summary = "간편 결제 승인 API", description = "REQUEST HEADER에 Authorizaion : c674ccc0-2f95-11ee-bf23-f53b19ae9a8d 셋팅하셔야합니다. \n 카드 등록 후 결제 승인 API입니다.")
    @Parameters({
            @Parameter(name = "merchantId", description = "가맹점 ID", example = "merchant01"),
            @Parameter(name = "orderName", description = "주문자명", example = "홍길동"),
            @Parameter(name = "productName", description = "상품명", example = "테스트상품"),
            @Parameter(name = "amount", description = "금액", example = "1000"),
            @Parameter(name = "installment", description = "할부개월수", example = "0"),
            @Parameter(name = "billingToken", description = "카드 등록 후 발급받은 TOKEN", example = "2186429000057767")
    })
    @PostMapping("/api/v1/wallet/pay")
    public Object billing(@RequestBody @Validated ClientWalletPayRequestDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request){
        return walletService.pay(clientRequestDTO, request);
    }
}
