package hhplus.concert.api.presentation.response;

import hhplus.concert.application.concert.dto.ConcertServiceDto;
import hhplus.concert.support.type.SeatStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ConcertResponse {
    public record Concert(
            Long concertId,
            String title,
            String description
    ) {
        public static Concert from(ConcertServiceDto.Concert concertDto) {
            return new Concert(
                    concertDto.concertId(),
                    concertDto.title(),
                    concertDto.description()
            );
        }
    }

    public record Schedule(
            Long concertId,
            List<ConcertSchedule> concertSchedules
    ) {
        public static Schedule from(ConcertServiceDto.Schedule scheduleDto) {
            return new Schedule(
                    scheduleDto.concertId(),
                    scheduleDto.concertSchedules().stream()
                            .map(ConcertResponse.ConcertSchedule::from)
                            .collect(Collectors.toList())
            );
        }
    }

    public record ConcertSchedule(
            Long scheduleId,
            LocalDateTime concertAt,
            LocalDateTime reservationAt
    ) {
        public static ConcertSchedule from(ConcertServiceDto.ConcertSchedule concertScheduleDto) {
            return new ConcertSchedule(
                    concertScheduleDto.scheduleId(),
                    concertScheduleDto.concertAt(),
                    concertScheduleDto.reservationAt()
            );
        }
    }

    public record AvailableSeat(
            Long concertId,
            List<Seat> seats
    ) {
        public static AvailableSeat from(ConcertServiceDto.AvailableSeat availableSeatDto) {
            return new AvailableSeat(
                    availableSeatDto.concertId(),
                    availableSeatDto.seats().stream()
                            .map(ConcertResponse.Seat::from)
                            .collect(Collectors.toList())
            );
        }
    }

    public record Seat(
           Long concertId,
           int seatNumver,
           SeatStatus seatStatus,
           int seatPrice
   ) {
        public static Seat from(ConcertServiceDto.Seat seatDto) {
            return new Seat(
                    seatDto.seatId(),
                    seatDto.seatNumber(),
                    seatDto.seatStatus(),
                    seatDto.seatPrice()
            );
        }
    }
}
