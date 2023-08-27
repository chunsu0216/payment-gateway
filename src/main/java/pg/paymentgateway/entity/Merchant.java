package pg.paymentgateway.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PG_MERCHANT")
@Getter
@Setter
public class Merchant extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String merchantId;
    private String merchantName;
    private String paymentKey;

    @OneToMany(mappedBy = "merchant")
    private List<Van> vans = new ArrayList<>();

    @OneToMany(mappedBy = "merchant")
    private List<Pay> pays = new ArrayList<>();
}
