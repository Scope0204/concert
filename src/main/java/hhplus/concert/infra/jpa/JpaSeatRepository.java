package hhplus.concert.infra.jpa;

import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.support.type.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface JpaSeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByConcertScheduleId(Long concertScheduleId);

    @Modifying
    @Query("UPDATE Seat seat SET seat.status = :seatStatus WHERE seat.id = :seatId")
    void updateStatusById(@Param("seatId") Long seatId, @Param("seatStatus") SeatStatus seatStatus);

    @Modifying
    @Query("UPDATE Seat seat SET seat.status = :seatStatus WHERE seat.id IN :seatIds")
    void updateAllStatusByIds(@Param("seatIds") List<Long> seatIds, @Param("seatStatus") SeatStatus seatStatus);


}
