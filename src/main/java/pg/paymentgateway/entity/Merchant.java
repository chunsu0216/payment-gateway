package pg.paymentgateway.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PG_MERCHANT")
@Getter
public class Merchant extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String merchantId;
    private String merchantName;
    private String paymentKey;
}
