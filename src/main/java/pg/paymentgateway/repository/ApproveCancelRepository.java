package pg.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pg.paymentgateway.entity.ApproveCancel;

public interface ApproveCancelRepository extends JpaRepository<ApproveCancel, Long> {
}
