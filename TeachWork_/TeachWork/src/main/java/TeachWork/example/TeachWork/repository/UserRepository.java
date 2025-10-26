package TeachWork.example.TeachWork.repository;

import TeachWork.example.TeachWork.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //Login için

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
