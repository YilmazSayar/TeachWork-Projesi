package TeachWork.example.TeachWork.repository;

import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
    List<Room> findByMembersContaining(User user);
    List<Room> findByCreatedBy(User user);
    boolean existsByCode(String code);
} 