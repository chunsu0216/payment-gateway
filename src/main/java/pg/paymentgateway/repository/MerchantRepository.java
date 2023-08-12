package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Merchant findMerchantByPaymentKey(String paymentKey);
    Merchant findMerchantByMerchantId(String merchantId);
}
