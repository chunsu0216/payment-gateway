package pg.paymentgateway.controller;

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

@RestController
@Slf4j
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/api/v1/wallet/register")
    public Object register(@RequestBody @Validated ClientWalletRegisterDTO clientRequestDTO, BindingResult bindingResult){
        return walletService.register(clientRequestDTO);
    }

    @PostMapping("/api/v1/wallet/pay")
    public Object billing(@RequestBody @Validated ClientWalletPayRequestDTO clientRequestDTO, BindingResult bindingResult){
        return walletService.pay(clientRequestDTO);
    }
}
