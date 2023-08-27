package pg.paymentgateway.controller;

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

@RestController
@RequiredArgsConstructor
@Slf4j
public class KeyInController {

    private final KeyInService keyInService;

    @PostMapping(value = {"/api/v1/card/old-certification", "/api/v1/card/non-certification"})
    public Object keyIn(@RequestBody @Validated ClientKeyInRequestDTO clientRequestDTO, BindingResult bindingResult, HttpServletRequest request) {

        String method = ""; // 구인증/비인증 구분

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        // URI 별 분기 처리
        if(request.getServletPath().equals("/api/v1/card/old-certification")){
            method = "old-keyIn";
        }else if(request.getServletPath().equals("/api/v1/card/non-certification")){
            method = "non-keyIn";
        }

        return keyInService.keyIn(clientRequestDTO, method);
    }

    @PostMapping("/api/v1/card/cancel")
    public Object cancel(@RequestBody @Validated ClientKeyInCancelDTO clientRequestDTO, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        return keyInService.cancel(clientRequestDTO);
    }
}
