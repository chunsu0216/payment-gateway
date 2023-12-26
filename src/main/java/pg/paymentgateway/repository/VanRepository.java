package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pg.paymentgateway.entity.Van;

import java.util.List;

public interface VanRepository extends JpaRepository<Van, Long> {
    Van findVanByVanId(String vanId);

    @Query("select distinct van from Van van join fetch van.merchant")
    List<Van> findVanFetch(String merchantId);
}
