package TeachWork.example.TeachWork.repository;

import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy WHERE t.assignedTo = ?1")
    List<Task> findByAssignedTo(User user);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedTo WHERE t.assignedBy = ?1")
    List<Task> findByAssignedBy(User user);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedBy LEFT JOIN FETCH t.assignedTo WHERE t.roomId = ?1")
    List<Task> findByRoomId(Long roomId);
}
