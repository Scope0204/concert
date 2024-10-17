package hhplus.concert.domain.user.components;

import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserInfo(Long userId){
        User user = userRepository.findById(userId);
        return user;
    }
}
