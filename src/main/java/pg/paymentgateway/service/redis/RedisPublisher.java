package pg.paymentgateway.service.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import pg.paymentgateway.entity.Notification;

@Service
@AllArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(ChannelTopic topic, Notification notification){
        redisTemplate.convertAndSend(topic.getTopic(), notification);
    }
}
