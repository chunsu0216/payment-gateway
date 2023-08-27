package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.BillingToken;

public interface BillingTokenRepository extends JpaRepository<BillingToken, Long> {
}
