package pg.paymentgateway.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PG_CLIENT_REQUEST_CANCEL")
public class ClientCancelRequest {

    @Id @GeneratedValue
    private Long idx;
    private String merchantId;
    private String orderNumber;
    private String transactionId;
    private String partialCancelType;
    private Long amount;
}
