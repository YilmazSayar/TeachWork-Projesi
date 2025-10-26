package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Report;
import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report saveReport(Report report) {
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(java.time.LocalDateTime.now());
        }
        return reportRepository.save(report);
    }

    public List<Report> getReportsByTask(Task task) {
        return reportRepository.findByTask(task);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
