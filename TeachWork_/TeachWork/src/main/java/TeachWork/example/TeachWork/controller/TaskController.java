package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.TaskStatus;
import TeachWork.example.TeachWork.model.TaskPriority;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.model.Group;
import TeachWork.example.TeachWork.service.TaskServiceImpl;
import TeachWork.example.TeachWork.service.UserService;
import TeachWork.example.TeachWork.service.RoomService;
import TeachWork.example.TeachWork.service.PdfAnalysisService;
import TeachWork.example.TeachWork.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private static final String UPLOAD_DIR = "uploads/tasks/";

    @Autowired
    private TaskServiceImpl taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private PdfAnalysisService pdfAnalysisService;

    @Autowired
    private GroupRepository groupRepository;

    @GetMapping
    public String getTasks(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        List<Task> tasks = taskService.getTasksForUser(currentUser);
        List<Group> userGroups = groupRepository.findByMembersContaining(currentUser);
        List<User> users = userService.findAll(); // Tüm kullanıcıları getir
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("groups", userGroups);
        model.addAttribute("users", users);
        return "tasks";
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getTasks() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            List<Task> tasks = taskService.getTasksForUser(currentUser);
            logger.info("Kullanıcı için {} görev bulundu: {}", tasks.size(), currentUser.getEmail());
            
            // Görevleri ve ilişkili verileri içeren bir DTO listesi oluştur
            List<Map<String, Object>> taskDTOs = tasks.stream()
                .map(task -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", task.getId());
                    dto.put("title", task.getTitle());
                    dto.put("description", task.getDescription());
                    dto.put("status", task.getStatus());
                    dto.put("priority", task.getPriority());
                    dto.put("dueDate", task.getDueDate());
                    
                    // Atayan kullanıcı bilgileri
                    if (task.getAssignedBy() != null) {
                        Map<String, Object> assignedBy = new HashMap<>();
                        assignedBy.put("id", task.getAssignedBy().getId());
                        assignedBy.put("email", task.getAssignedBy().getEmail());
                        assignedBy.put("fullName", task.getAssignedBy().getFullName());
                        dto.put("assignedBy", assignedBy);
                    }
                    
                    // Atanan kullanıcı bilgileri
                    if (task.getAssignedTo() != null) {
                        Map<String, Object> assignedTo = new HashMap<>();
                        assignedTo.put("id", task.getAssignedTo().getId());
                        assignedTo.put("email", task.getAssignedTo().getEmail());
                        assignedTo.put("fullName", task.getAssignedTo().getFullName());
                        dto.put("assignedTo", assignedTo);
                    }

                    // Dosya bilgileri
                    if (task.getFileName() != null) {
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", task.getFileName());
                        fileInfo.put("fileType", task.getFileType());
                        dto.put("fileInfo", fileInfo);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(taskDTOs);
        } catch (Exception e) {
            logger.error("Görevler listelenirken hata oluştu", e);
            return ResponseEntity.badRequest().body("Görevler listelenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/room/{roomId}/users")
    @ResponseBody
    public ResponseEntity<?> getRoomUsers(@PathVariable Long roomId) {
        try {
            List<User> users = roomService.getRoomMembers(roomId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam("dueDate") String dueDateStr,
            @RequestParam("roomId") Long roomId,
            @RequestParam("assignedToId") Long assignedToId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            // Oda kontrolü
            Room room = roomService.getRoomById(roomId);
            if (!room.getMembers().contains(currentUser)) {
                return ResponseEntity.badRequest().body("Bu odaya erişim izniniz yok.");
            }
            
            // Atanacak kullanıcı kontrolü
            User assignedTo = userService.findById(assignedToId);
            if (!room.getMembers().contains(assignedTo)) {
                return ResponseEntity.badRequest().body("Seçilen kullanıcı bu odanın üyesi değil.");
            }
            
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(TaskPriority.valueOf(priority));
            task.setDueDate(LocalDateTime.parse(dueDateStr + "T00:00:00"));
            task.setAssignedBy(currentUser);
            task.setAssignedTo(assignedTo);
            task.setRoom(room);
            task.setStatus(TaskStatus.PENDING);
            
            // Dosya yükleme işlemi
            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                
                // Upload dizinini oluştur
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Dosyayı kaydet
                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(file.getInputStream(), filePath);
                
                task.setFileName(originalFileName);
                task.setFilePath(newFileName);
                task.setFileType(file.getContentType());
            }
            
            Task savedTask = taskService.createTask(task);
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            logger.error("Görev oluşturulurken hata oluştu", e);
            return ResponseEntity.badRequest().body("Görev oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/{taskId}/status")
    @ResponseBody
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long taskId, @RequestParam String status) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Görev bulunamadı"));
            
            // Sadece görevi atanan kişi durumu güncelleyebilir
            if (!task.getAssignedTo().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Bu görevin durumunu güncelleme yetkiniz yok");
            }
            
            TaskStatus newStatus = TaskStatus.valueOf(status);
            Task updatedTask = taskService.updateTaskStatus(taskId, newStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Görev durumu başarıyla güncellendi");
            response.put("task", updatedTask);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Görev durumu güncellenirken hata oluştu: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Görev durumu güncellenirken hata oluştu", e);
            return ResponseEntity.badRequest().body("Görev durumu güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    @ResponseBody
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Görev bulunamadı"));
            
            // Sadece görevi oluşturan veya atanan kişi silebilir
            if (!task.getAssignedBy().getId().equals(currentUser.getId()) && 
                !task.getAssignedTo().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Bu görevi silme yetkiniz yok");
            }
            
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Görev bulunamadı: {}", taskId);
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Görev silinirken hata oluştu", e);
            return ResponseEntity.badRequest().body("Görev silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/download/{taskId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long taskId) {
        try {
            Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Görev bulunamadı"));
            
            if (task.getFilePath() == null) {
                return ResponseEntity.badRequest().body("Bu görev için dosya bulunamadı");
            }
            
            Path filePath = Paths.get(UPLOAD_DIR + task.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body("Dosya bulunamadı");
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", task.getFileName());
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Dosya indirilirken hata oluştu", e);
            return ResponseEntity.badRequest().body("Dosya indirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/analyze/{taskId}")
    @ResponseBody
    public ResponseEntity<?> analyzePdf(@PathVariable Long taskId) {
        try {
            Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Görev bulunamadı"));
            
            if (task.getFilePath() == null) {
                return ResponseEntity.badRequest().body("Bu görev için dosya bulunamadı");
            }
            
            Path filePath = Paths.get(UPLOAD_DIR + task.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body("Dosya bulunamadı");
            }
            
            List<String> suggestions = pdfAnalysisService.analyzePdf(filePath.toString());
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("PDF analizi sırasında hata oluştu", e);
            return ResponseEntity.badRequest().body("PDF analizi sırasında bir hata oluştu: " + e.getMessage());
        }
    }
}
