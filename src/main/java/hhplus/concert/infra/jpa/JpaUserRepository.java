package hhplus.concert.infra.jpa;

import hhplus.concert.domain.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {
}

