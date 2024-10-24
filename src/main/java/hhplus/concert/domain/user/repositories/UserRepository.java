package hhplus.concert.domain.user.repositories;


import hhplus.concert.domain.user.models.User;

import java.util.List;

public interface UserRepository {
    List<User> findByAll();
    User findById(Long userId);
    void save(User user);
}
