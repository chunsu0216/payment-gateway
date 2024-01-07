package pg.paymentgateway.service.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import pg.paymentgateway.entity.Notification;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;

    /**
     * Redis Subscriber 수신
     * 가맹점 노티 수신 테스트 용도
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Notification notification = objectMapper.readValue(message.getBody(), Notification.class);
            log.info("notification subscribe : {}", notification);
        } catch (IOException e) {
            log.error("error : {}", e);
        }
    }
}
