package pg.paymentgateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pg.paymentgateway.dto.ClientRequestDTO;
import pg.paymentgateway.service.KeyInService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class KeyInController {

    private final KeyInService keyInService;

    @PostMapping("/api/v1/card/old-certification")
    public Object keyIn(@RequestBody @Validated ClientRequestDTO clientRequestDTO, BindingResult bindingResult) {
        log.info("request : {}", clientRequestDTO.toString());

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        keyInService.oldCertification(clientRequestDTO);

        return null;
    }
}
