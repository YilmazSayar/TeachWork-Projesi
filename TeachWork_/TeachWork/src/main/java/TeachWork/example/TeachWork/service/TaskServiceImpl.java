package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.TaskStatus;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Optional<Task> updateTask(Long id, Task updatedTask) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setStatus(updatedTask.getStatus());
            return Optional.of(taskRepository.save(task));
        }
        return Optional.empty();
    }

    @Override
    public Task assignTask(Task task, Long assignToUserId, Long assignByUserId) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<Task> getTasksForUser(User user) {
        try {
            logger.info("Kullanıcı için görevler getiriliyor: {}", user.getEmail());
            List<Task> tasks = taskRepository.findByAssignedTo(user);
            logger.info("Bulunan görev sayısı: {}", tasks.size());
            return tasks;
        } catch (Exception e) {
            logger.error("Kullanıcı görevleri getirilirken hata oluştu", e);
            throw e;
        }
    }

    @Override
    public List<Task> getTasksCreatedByUser(User user) {
        return taskRepository.findByAssignedBy(user);
    }

    @Override
    public Task createTask(Task task) {
        try {
            logger.info("Görev oluşturuluyor: {}", task);
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Görev başlığı boş olamaz");
            }
            if (task.getAssignedTo() == null) {
                throw new IllegalArgumentException("Görev atanacak kullanıcı belirtilmedi");
            }
            if (task.getAssignedBy() == null) {
                throw new IllegalArgumentException("Görevi oluşturan kullanıcı belirtilmedi");
            }
            
            Task savedTask = taskRepository.save(task);
            logger.info("Görev başarıyla oluşturuldu: {}", savedTask);
            return savedTask;
        } catch (Exception e) {
            logger.error("Görev oluşturulurken hata oluştu", e);
            throw e;
        }
    }

    @Override
    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        try {
            logger.info("Görev durumu güncelleniyor. TaskId: {}, Yeni Durum: {}", taskId, status);
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Görev bulunamadı: " + taskId));
            
            task.setStatus(status);
            Task updatedTask = taskRepository.save(task);
            logger.info("Görev durumu başarıyla güncellendi: {}", updatedTask);
            return updatedTask;
        } catch (Exception e) {
            logger.error("Görev durumu güncellenirken hata oluştu", e);
            throw e;
        }
    }

    public List<Task> getTasksByRoomId(Long roomId) {
        try {
            logger.info("Oda için görevler getiriliyor. RoomId: {}", roomId);
            List<Task> tasks = taskRepository.findByRoomId(roomId);
            logger.info("Bulunan görev sayısı: {}", tasks.size());
            return tasks;
        } catch (Exception e) {
            logger.error("Oda görevleri getirilirken hata oluştu", e);
            throw e;
        }
    }
}
