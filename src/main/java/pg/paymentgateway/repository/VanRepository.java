package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.Van;

public interface VanRepository extends JpaRepository<Van, Long> {
    Van findVanByVanId(String vanId);
}
