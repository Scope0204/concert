package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.response.QueueResponse;
import hhplus.concert.application.queue.usecase.QueueFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
public class QueueController {

    private final QueueFacade queueFacade;

    public QueueController(QueueFacade queueFacade) {
        this.queueFacade = queueFacade;
    }

    // 대기열에 사용자를 추가하고 대기열 토큰을 반환하도록 합니다.
    @PostMapping("/token/users/")
    public QueueResponse.Token issueQueueToken(
            @RequestHeader("User-Id") Long userId) {
        return QueueResponse.Token.from(queueFacade.issueQueueToken(userId));
    }

    // 유저 토큰을 통해 사용자의 대기열 상태를 조회합니다.
    @GetMapping("/status")
    public QueueResponse.Queue getQueueStatus(
            @RequestHeader("TOKEN") String token) {
        return QueueResponse.Queue.from(queueFacade.findQueueByToken(token));
    }
}
