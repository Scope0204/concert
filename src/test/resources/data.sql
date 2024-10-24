-- Users
INSERT INTO users (name) VALUES
('John Doe'),
('Jane Smith'),
('Alice Johnson');

-- Balance
INSERT INTO balance (amount, updated_at, user_id) VALUES
(10000, '2024-01-01 10:00:00', 1),
(5000, '2024-01-02 11:00:00', 2),
(15000, '2024-01-03 12:00:00', 3);

-- Concert
INSERT INTO concert (concert_status, description, title) VALUES
(1, 'A great rock concert', 'Rock Fest 2024'),
(0, 'An upcoming jazz concert', 'Jazz Night 2024'),
(1, 'A classical symphony', 'Symphony Orchestra');


-- Concert Schedule
INSERT INTO concert_schedule (concert_at, concert_id, reservation_at) VALUES
('2024-06-01 18:00:00', 1, '2024-05-01 10:00:00'),
('2024-06-10 19:00:00', 2, '2024-05-05 12:00:00'),
('2024-06-15 20:00:00', 3, '2024-05-10 14:00:00');

-- 예약 가능한 시간이 아직 오지 않은 경우를 추가
INSERT INTO concert_schedule (concert_id, reservation_at, concert_at)
VALUES (1, '2024-11-01 12:00:00', '2024-11-15 18:00:00');

-- Seat(좌석 번호 1부터 25까지 이용 가능한 상태로 더미 데이터 생성)
INSERT INTO seat (seat_number, seat_price, concert_schedule_id, status) VALUES
(1, 10000, 1, 'AVAILABLE'),
(2, 10000, 1, 'AVAILABLE'),
(3, 10000, 1, 'AVAILABLE'),
(4, 10000, 1, 'AVAILABLE'),
(5, 10000, 1, 'AVAILABLE'),
(6, 10000, 1, 'AVAILABLE'),
(7, 10000, 1, 'AVAILABLE'),
(8, 10000, 1, 'AVAILABLE'),
(9, 10000, 1, 'AVAILABLE'),
(10, 10000, 1, 'AVAILABLE'),
(11, 10000, 1, 'AVAILABLE'),
(12, 10000, 1, 'AVAILABLE'),
(13, 10000, 1, 'AVAILABLE'),
(14, 10000, 1, 'AVAILABLE'),
(15, 10000, 1, 'AVAILABLE'),
(16, 10000, 1, 'AVAILABLE'),
(17, 10000, 1, 'AVAILABLE'),
(18, 10000, 1, 'AVAILABLE'),
(19, 10000, 1, 'AVAILABLE'),
(20, 10000, 1, 'AVAILABLE'),
(21, 10000, 1, 'AVAILABLE'),
(22, 10000, 1, 'AVAILABLE'),
(23, 10000, 1, 'AVAILABLE'),
(24, 10000, 1, 'AVAILABLE'),
(25, 10000, 1, 'AVAILABLE');

-- Seat(좌석 번호 26부터 50까지 사용 불가능 한 더미 데이터)
INSERT INTO seat (seat_number, seat_price, concert_schedule_id, status) VALUES
(26, 10000, 1, 'UNAVAILABLE'),
(27, 10000, 1, 'UNAVAILABLE'),
(28, 10000, 1, 'UNAVAILABLE'),
(29, 10000, 1, 'UNAVAILABLE'),
(30, 10000, 1, 'UNAVAILABLE'),
(31, 10000, 1, 'UNAVAILABLE'),
(32, 10000, 1, 'UNAVAILABLE'),
(33, 10000, 1, 'UNAVAILABLE'),
(34, 10000, 1, 'UNAVAILABLE'),
(35, 10000, 1, 'UNAVAILABLE'),
(36, 10000, 1, 'UNAVAILABLE'),
(37, 10000, 1, 'UNAVAILABLE'),
(38, 10000, 1, 'UNAVAILABLE'),
(39, 10000, 1, 'UNAVAILABLE'),
(40, 10000, 1, 'UNAVAILABLE'),
(41, 10000, 1, 'UNAVAILABLE'),
(42, 10000, 1, 'UNAVAILABLE'),
(43, 10000, 1, 'UNAVAILABLE'),
(44, 10000, 1, 'UNAVAILABLE'),
(45, 10000, 1, 'UNAVAILABLE'),
(46, 10000, 1, 'UNAVAILABLE'),
(47, 10000, 1, 'UNAVAILABLE'),
(48, 10000, 1, 'UNAVAILABLE'),
(49, 10000, 1, 'UNAVAILABLE'),
(50, 10000, 1, 'UNAVAILABLE');





