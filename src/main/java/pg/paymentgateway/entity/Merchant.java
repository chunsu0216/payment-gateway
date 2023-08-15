package pg.paymentgateway.entity;

import lombok.Getter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PG_MERCHANT")
@Getter
public class Merchant extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String merchantId;
    private String merchantName;
    private String paymentKey;

    @OneToMany(mappedBy = "merchant")
    private List<Van> vans = new ArrayList<>();
}
