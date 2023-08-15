package pg.paymentgateway.repository.query;

import pg.paymentgateway.entity.Merchant;

public interface MerchantRepositoryCustom {
    Merchant search(String method, String vanId);
}
