package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.Pay;

public interface PayRepository extends JpaRepository<Pay, Long> {

    Pay findPayByTransactionIdOrOrderId(String transactionId, String orderId);
}
