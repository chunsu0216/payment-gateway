package pg.paymentgateway.entity;

import lombok.Getter;
import javax.persistence.*;
@Entity
@Table(name = "PG_VAN")
@Getter
public class Van extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String van;
    private String vanId;
    private String method;
    private String vanKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID")
    private Merchant merchant;
}
