package hhplus.concert.domain.user.repositories;


import hhplus.concert.domain.user.models.User;

public interface UserRepository {
    User findById(Long userId);
}
