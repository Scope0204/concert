package hhplus.concert.application.concert.usecase;

import hhplus.concert.application.concert.dto.ConcertServiceDto;
import hhplus.concert.domain.concert.components.ConcertService;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.support.config.CacheConfig;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcertFacade {
    private final QueueService queueService;
    private final ConcertService concertService;

    public ConcertFacade(QueueService queueService, ConcertService concertService) {
        this.queueService = queueService;
        this.concertService = concertService;
    }
    /**
     * 콘서트 목록을 조회하여 반환합니다.
     * 토큰을 통해 대기열 상태를 검증하도록 합니다.
     */
    @Cacheable(
            cacheNames = {CacheConfig.CACHE_TEN_MIN},
            key = "'v1-concert-' + #token",
            condition = "#token != null",
            sync = true
    )
    public List<ConcertServiceDto.Concert> getAvailableConcerts(String token){
        validateQueueStatus(token);

        return concertService.getAvailableConcerts().stream()
                .map(concert -> new ConcertServiceDto.Concert(
                        concert.getId(),
                        concert.getTitle(),
                        concert.getDescription()
                ))
                .collect(Collectors.toList());
    }
    /**
     * 특정 콘서트의 예약 가능한 일정 목록을 조회하여 반환합니다.
     * 토큰을 통해 대기열 상태를 검증하도록 합니다.
     * 현재 예약이 가능한 전체 콘서트 리스트를 Schedule dto 로 변환하여 리턴합니다.
     */
    @Cacheable(
            cacheNames = {CacheConfig.CACHE_TEN_MIN},
            key = "'v1-concert-' + #concertId",
            condition = "#token != null && #concertId != null",
            sync = true
    )
    public ConcertServiceDto.Schedule getAvailableSchedulesForConcert(Long concertId, String token) {
        validateQueueStatus(token);

        List<ConcertServiceDto.ConcertSchedule> availableConcertSchedules = concertService
                .getAvailableSchedulesForConcert(concertId)
                .stream()
                .map(it -> new ConcertServiceDto.ConcertSchedule(
                        it.getId(),
                        it.getConcertAt(),
                        it.getReservationAt()
                ))
                .collect(Collectors.toList());

        return new ConcertServiceDto.Schedule(concertId, availableConcertSchedules);
    }

    /**
     * 콘서트와 날짜 정보를 입력받아 예약가능한 좌석정보를 조회합니다.
     * 토큰을 통해 대기열 상태를 검증하도록 합니다.
     */
    @Cacheable(
            cacheNames = {CacheConfig.CACHE_ONE_MIN},
            key = "'v1-concert-' + #concertId + '-scheduleId-' + #scheduleId",
            condition = "#token != null && #concertId != null && #scheduleId != null",
            sync = true
    )
    public ConcertServiceDto.AvailableSeat getAvailableSeats(Long concertId, Long scheduleId, String token){
        validateQueueStatus(token);

        List<ConcertServiceDto.Seat> availableConcertSeats = concertService
                .getAvailableSeats(concertId, scheduleId)
                .stream()
                .map(it -> new ConcertServiceDto.Seat(
                        it.getId(),
                        it.getSeatNumber(),
                        it.getStatus(),
                        it.getSeatPrice()
                ))
                .collect(Collectors.toList());

        return new ConcertServiceDto.AvailableSeat(concertId, availableConcertSeats);
    }

    /**
     * 토큰을 통해 대기열 상태를 검증
     */
    private void validateQueueStatus(String token){
        QueueStatus queueStatus = queueService.getQueueStatus(token);
        if(queueStatus != QueueStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUEUE_NOT_ALLOWED);
        }
    }

}
