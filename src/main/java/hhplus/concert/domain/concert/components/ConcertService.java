package hhplus.concert.domain.concert.components;

import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.SeatStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;

    public ConcertService(ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, SeatRepository seatRepository) {
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * 예약이 가능한 콘서트 리스트를 조회
     * @return
     */
    public List<Concert> getAvailableConcerts(){
        // 예약 가능한 콘서트 정보만 필터링
        return concertRepository.findAll().stream()
                .filter(concert -> concert.getConcertStatus() == ConcertStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * 예약 가능한 콘서트 날짜 리스트를 조회. 이전에 검증과정을 거침
     * 1. 콘서트가 예약 가능한지 검증
     * @param concertId
     * @return
     */
    public List<ConcertSchedule> getAvailableSchedulesForConcert(Long concertId){
        validateConcertStatus(concertId);

        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findAllByConcertId(concertId);
        return concertSchedules.stream()
                .filter(schedule -> validateWithinReservationPeriod(schedule.getReservationAt(), schedule.getConcertAt()))
                .collect(Collectors.toList());
    }

    /**
     * 예약가능한 좌석정보를 조회. 이전에 검증과정을 거침
     * 1. 콘서트가 예약 가능한지 검증
     * 2. 콘서트 스케줄이 유효한지 검증
     * @param concertScheduleId
     * @return
     */
    public List<Seat> getAvailableSeats(Long concertId, Long concertScheduleId) {

        validateConcertStatus(concertId);

        ConcertSchedule concertSchedule = concertScheduleRepository.findById(concertScheduleId);
        if (!validateWithinReservationPeriod(concertSchedule.getReservationAt(), concertSchedule.getConcertAt())) {
            throw new BusinessException(ErrorCode.CONCERT_UNAVAILABLE);
        }

        // 예약 가능한 좌석정보만 필터링
        return seatRepository.findAllByConcertScheduleId(concertScheduleId).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    // 예약 가능한 콘서트 인지 검증
    private void validateConcertStatus(Long concertId){
        Concert concert = concertRepository.findById(concertId);
        if(concert.getConcertStatus() == ConcertStatus.UNAVAILABLE) {
            throw new BusinessException(ErrorCode.CONCERT_UNAVAILABLE);
        }
    }
    // 예약 가능한 시간대 범위에 있는지 검증
    private boolean validateWithinReservationPeriod(LocalDateTime reservationAt, LocalDateTime concertAt) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(reservationAt) && now.isBefore(concertAt);
    }
}
