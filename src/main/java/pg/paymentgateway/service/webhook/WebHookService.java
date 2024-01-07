package pg.paymentgateway.service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pg.paymentgateway.entity.Notification;
import pg.paymentgateway.repository.NotificationRepository;
import pg.paymentgateway.service.redis.RedisPublisher;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebHookService {
    private final ChannelTopic channelTopic;
    private final RedisPublisher redisPublisher;
    private final NotificationRepository notificationRepository;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void webhook() {
        log.info("::: webhook service start :::");
        List<Notification> notifications = notificationRepository.findByRetryCount();
        notifications.forEach(notification -> {
            redisPublisher.publish(channelTopic, notification);
            notification.updateRetryCount();
        });
    }
}
