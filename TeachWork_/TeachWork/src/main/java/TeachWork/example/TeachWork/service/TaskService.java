package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.TaskStatus;
import TeachWork.example.TeachWork.model.User;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task saveTask(Task task);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long id);
    void deleteTask(Long id);
    Optional<Task> updateTask(Long id, Task updatedTask);
    Task assignTask(Task task, Long assignToUserId, Long assignByUserId);
    List<Task> getTasksForUser(User user);
    List<Task> getTasksCreatedByUser(User user);
    Task createTask(Task task);
    Task updateTaskStatus(Long taskId, TaskStatus newStatus);
    List<Task> getTasksByRoomId(Long roomId);
}
