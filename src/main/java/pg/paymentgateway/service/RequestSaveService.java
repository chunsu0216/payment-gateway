package pg.paymentgateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pg.paymentgateway.dto.ClientKeyInCancelDTO;
import pg.paymentgateway.dto.ClientKeyInRequestDTO;
import pg.paymentgateway.dto.ClientWalletPayRequestDTO;
import pg.paymentgateway.dto.ClientWalletRegisterDTO;
import pg.paymentgateway.entity.ClientRequest;
import pg.paymentgateway.entity.Van;
import pg.paymentgateway.repository.ClientRequestRepository;

@Service
@RequiredArgsConstructor
public class RequestSaveService {

    private final ClientRequestRepository clientRequestRepository;

    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public void saveKeyInRequest(ClientKeyInRequestDTO requestDTO, Van van) {
        clientRequestRepository.save(setKeyInClientRequest((requestDTO), van));
    }

    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public void saveCancelRequest(ClientKeyInCancelDTO clientRequestDTO) {
        clientRequestRepository.save(setCancelRequest(clientRequestDTO));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveWalletRequest(ClientWalletRegisterDTO clientRequestDTO, Van van) {
        clientRequestRepository.save(setWalletRegisterClientRequest(clientRequestDTO, van));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveWalletPayRequest(ClientWalletPayRequestDTO clientRequestDTO, Van van) {
        clientRequestRepository.save(setWalletPayClientRequest(clientRequestDTO, van));
    }

    private ClientRequest setWalletPayClientRequest(ClientWalletPayRequestDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .amount(clientRequestDTO.getAmount())
                .installment(clientRequestDTO.getInstallment())
                .billingToken(clientRequestDTO.getBillingToken())
                .van(van.getVan())
                .vanId(van.getVanId())
                .build();
    }

    /**
     * CLIENT REQUEST 내역 SAVE SETTING
     * @param clientRequestDTO
     * @param van
     * @return
     */
    private ClientRequest setWalletRegisterClientRequest(ClientWalletRegisterDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .van(van.getVan())
                .vanId(van.getVanId())
                .build();
    }


    private ClientRequest setCancelRequest(ClientKeyInCancelDTO clientRequestDTO) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .transactionType("cancel")
                .rootTransactionId(clientRequestDTO.getTransactionId())
                .orderId(clientRequestDTO.getOrderId())
                .amount(clientRequestDTO.getAmount())
                .build();
    }

    /**
     * CLIENT REQUEST 내역 SAVE SETTING
     * @param clientRequestDTO
     * @param van
     * @return
     */
    private ClientRequest setKeyInClientRequest(ClientKeyInRequestDTO clientRequestDTO, Van van) {
        return new ClientRequest().builder()
                .merchantId(clientRequestDTO.getMerchantId())
                .transactionType("keyIn")
                .orderId(clientRequestDTO.getOrderId())
                .orderName(clientRequestDTO.getOrderName())
                .productName(clientRequestDTO.getProductName())
                .amount(clientRequestDTO.getAmount())
                .cardNumber(clientRequestDTO.getCardNumber())
                .expireDate(clientRequestDTO.getExpireDate())
                .installment(clientRequestDTO.getInstallment())
                .password(clientRequestDTO.getPassword())
                .userInfo(clientRequestDTO.getUserInfo())
                .van(van.getVan())
                .vanId(van.getVanId())
                .build();
    }
}
