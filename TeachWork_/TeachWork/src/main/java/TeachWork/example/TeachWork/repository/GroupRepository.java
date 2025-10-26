package TeachWork.example.TeachWork.repository;

import TeachWork.example.TeachWork.model.Group;
import TeachWork.example.TeachWork.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMembersContaining(User user);
} 