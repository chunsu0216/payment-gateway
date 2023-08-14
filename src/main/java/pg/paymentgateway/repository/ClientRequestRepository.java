package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.ClientRequest;

public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {

    ClientRequest findClientRequestByOrderId(String orderId);
}
