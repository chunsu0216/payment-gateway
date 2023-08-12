package pg.paymentgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pg.paymentgateway.dto.ClientRequestDTO;
import pg.paymentgateway.entity.Merchant;
import pg.paymentgateway.exception.ForbiddenException;
import pg.paymentgateway.repository.MerchantRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyInService {

    private final MerchantRepository merchantRepository;
    private static final String INVALID_EXPIRE_DATE = "올바르지않은 유효기간입니다.";
    private static final String FORBIDDEN_MERCHANT = "존재하지않은 가맹점 ID입니다.";

    @Transactional
    public void oldCertification(ClientRequestDTO clientRequestDTO) {

        // 유효기간 검증
        if(!this.validationExpireDate(clientRequestDTO.getExpireDate())){
            throw new IllegalArgumentException(INVALID_EXPIRE_DATE);
        }

        // 가맹점 ID 검증
        Optional<Merchant> merchantByMerchantId = Optional.ofNullable(merchantRepository.findMerchantByMerchantId(clientRequestDTO.getMerchantId()));
        if(merchantByMerchantId.isEmpty()){
            throw new ForbiddenException(FORBIDDEN_MERCHANT);
        }else{
            // CLIENT REQUEST DB INSERT
        }

    }

    /**
     * 현재 날짜 기준으로 YYMM 비교 메소드
     * @param expireDate
     * @return
     */
    private boolean validationExpireDate(String expireDate) {
        String yyMM = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));

        if(Integer.parseInt(expireDate) < Integer.parseInt(yyMM)){
            return false;
        }
        return true;
    }
}
