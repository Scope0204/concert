package hhplus.concert.infra.impl;

import hhplus.concert.support.exception.NotFoundException;
import hhplus.concert.infra.jpa.JpaUserRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
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
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
    }
}
