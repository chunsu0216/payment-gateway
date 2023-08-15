package pg.paymentgateway.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import pg.paymentgateway.entity.Merchant;
import pg.paymentgateway.entity.QMerchant;

import javax.persistence.EntityManager;

public class MerchantRepositoryImpl implements MerchantRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MerchantRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    @Override
    public Merchant search(String method, String vanId) {
        QMerchant merchant = new QMerchant("merchant");

       /* return queryFactory.selectFrom(merchant)
                .where(merchant.van.vanId.eq(vanId)
                        .and(merchant.van.method.eq(method)))
                .fetchOne();*/

        return null;

    }
}
