package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.dto.ReportDTO;
import TeachWork.example.TeachWork.model.Report;
import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.service.ReportService;
import TeachWork.example.TeachWork.service.TaskServiceImpl;
import TeachWork.example.TeachWork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final TaskServiceImpl taskService;
    private final UserService userService;

    @Autowired
    public ReportController(ReportService reportService, TaskServiceImpl taskService, UserService userService) {
        this.reportService = reportService;
        this.taskService = taskService;
        this.userService = userService;
    }

    // Yeni rapor oluştur - DTO ile
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportDTO reportDTO) {
        try {
            Task task = taskService.getTaskById(reportDTO.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Görev bulunamadı: " + reportDTO.getTaskId()));
            
            User user = userService.getUserById(reportDTO.getUploadedById());

            Report report = new Report();
            report.setTitle(reportDTO.getTitle());
            report.setContent(reportDTO.getContent());
            report.setTask(task);
            report.setUploadedBy(user);
            report.setUploadedAt(LocalDateTime.now());
            report.setCreatedAt(LocalDateTime.now());

            Report savedReport = reportService.saveReport(report);
            return ResponseEntity.ok(savedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Tüm raporları getir
    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    // Belirli bir görevin raporlarını getir
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Report>> getReportsByTask(@PathVariable Long taskId) {
        Optional<Task> task = taskService.getTaskById(taskId);
        if (task.isPresent()) {
            List<Report> reports = reportService.getReportsByTask(task.get());
            return ResponseEntity.ok(reports);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ID ile rapor getir
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Optional<Report> report = reportService.getReportById(id);
        return report.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Rapor sil
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Rapor silinirken bir hata oluştu: " + e.getMessage());
        }
    }
}
