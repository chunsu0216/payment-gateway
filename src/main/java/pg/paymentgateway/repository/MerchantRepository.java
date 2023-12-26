package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pg.paymentgateway.entity.Merchant;

import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Merchant findMerchantByPaymentKey(String paymentKey);
    Merchant findMerchantByMerchantIdAndPaymentKey(String merchantId, String paymentKey);

    @Query("select distinct m from Merchant m join fetch m.vans")
    List<Merchant> findMerchantListFetch();
}
