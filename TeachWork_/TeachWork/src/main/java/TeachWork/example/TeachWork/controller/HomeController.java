package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.service.TaskService;
import TeachWork.example.TeachWork.service.UserService;
import TeachWork.example.TeachWork.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        
        // Kullanıcı bilgilerini ekle
        model.addAttribute("user", currentUser);
        
        // Son görevleri ekle (en son 5 görev)
        List<Task> recentTasks = taskService.getTasksForUser(currentUser).stream()
                .limit(5)
                .toList();
        model.addAttribute("recentTasks", recentTasks);
        
        // Kullanıcının odalarını ekle
        List<Room> rooms = roomService.getRoomsForUser(currentUser);
        model.addAttribute("rooms", rooms);
        
        return "home";
    }
} 