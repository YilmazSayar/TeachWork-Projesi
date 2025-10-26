package TeachWork.example.TeachWork.repository;

import TeachWork.example.TeachWork.model.Report;
import TeachWork.example.TeachWork.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByTask(Task task);
}
