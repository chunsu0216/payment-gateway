package pg.paymentgateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import pg.paymentgateway.service.van.VanService;
import pg.paymentgateway.service.van.ksnet.Ksnet;

@Configuration
public class VanServiceConfig {

    @Bean(name = "ksnet")
    public VanService ksnetService(){
        return new Ksnet(new RestTemplate(), new ObjectMapper());
    }
}
