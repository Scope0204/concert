package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.response.ConcertResponse;
import hhplus.concert.application.concert.usecase.ConcertFacade;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertFacade concertFacade;

    public ConcertController(ConcertFacade concertFacade) {
        this.concertFacade = concertFacade;
    }

    // 콘서트 목록을 조회 합니다.
    @GetMapping("")
    public List<ConcertResponse.Concert> getConcerts(
            @RequestHeader("TOKEN") String token) {
        return concertFacade
                .getAvailableConcerts(token)
                .stream()
                .map(ConcertResponse.Concert::from)
                .collect(Collectors.toList());
    }

    // 특정 콘서트의 예약 가능한 일정 목록을 조회합니다.
    @GetMapping("/{concertId}/schedules")
    public ConcertResponse.Schedule getConcertSchedules(
            @PathVariable Long concertId,
            @RequestHeader("TOKEN") String token) {

        return ConcertResponse.Schedule.from(concertFacade.getAvailableSchedulesForConcert(concertId, token));
    }

    // 해당 날짜의 콘서트 좌석 목록을 조회 합니다.
    @GetMapping("/{concertId}/schedules/{scheduleId}/seats")
    public ConcertResponse.AvailableSeat getConcertSeats(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId,
            @RequestHeader("TOKEN") String token) {

        return ConcertResponse.AvailableSeat.from(concertFacade.getAvailableSeats(concertId,scheduleId,token));
    }
}
