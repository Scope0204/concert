package hhplus.concert.api.presentation.scheduler;

import hhplus.concert.application.queue.usecase.QueueFacade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueScheduler {
    private final QueueFacade queueFacade;

    public QueueScheduler(QueueFacade queueFacade) {
        this.queueFacade = queueFacade;
    }

    @Scheduled(fixedRate = 60000)
    public void maintainActiveQueueCountWithScheduler() {
        queueFacade.updateToActiveTokens();
    }

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredActiveQueue() {
        queueFacade.cancelExpiredActiveQueue();
    }

}
