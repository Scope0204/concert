package hhplus.concert.infra.impl;

import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.infra.jpa.JpaUserRepository;
import hhplus.concert.support.error.exception.UserException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository{
    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User findById(Long userId) {
        return jpaUserRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFound() {
                });
    }
}
