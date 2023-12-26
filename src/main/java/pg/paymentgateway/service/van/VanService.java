package pg.paymentgateway.service.van;

import pg.paymentgateway.dto.ClientKeyInCancelDTO;
import pg.paymentgateway.dto.ClientKeyInRequestDTO;
import pg.paymentgateway.entity.Pay;
import pg.paymentgateway.entity.Van;

import java.util.Map;

public interface VanService {

    Map<String, Object> approveKeyIn(String transactionId, ClientKeyInRequestDTO clientRequest, Van van, String method);
    Map<String, Object> cancel(Pay pay, String cancelType, ClientKeyInCancelDTO clientRequestDTO, String method);
}
