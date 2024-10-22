package hhplus.concert.api.presentation.request;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;

public class ReservationRequest {
    public record Detail(
            Long userId,
            Long concertId,
            Long scheduleId,
            Long seatId
    ){}

    public static ReservationServiceDto.Request toDto(Detail request) {
        return new ReservationServiceDto.Request(
                request.userId(),
                request.concertId(),
                request.scheduleId(),
                request.seatId()
        );
    }
}
