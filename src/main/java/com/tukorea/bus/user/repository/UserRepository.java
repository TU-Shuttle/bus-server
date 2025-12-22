package com.tukorea.bus.user.repository;

import com.tukorea.bus.user.entity.User;
import com.tukorea.bus.user.repository.custom.UserRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends JpaRepository<User, String>, UserRepositoryCustom {

    Optional<User> findUserById(String id);

    boolean existsUserById(String id);
}
