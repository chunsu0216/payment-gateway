package pg.paymentgateway.entity;

import lombok.Getter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PG_VAN")
@Getter
public class Van extends BaseEntity{

    @Id @GeneratedValue
    private Long idx;
    private String van;
    private String vanId;
    private String vanKey;
}
