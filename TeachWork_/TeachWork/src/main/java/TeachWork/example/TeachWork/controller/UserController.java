package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.service.UserService;
import TeachWork.example.TeachWork.service.TaskService;
import TeachWork.example.TeachWork.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RoomService roomService;

    @PostMapping("/api/users/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");
            String name = payload.get("name");

            if (email == null || password == null || name == null) {
                return ResponseEntity.badRequest().body("Tüm alanları doldurun");
            }

            if (userService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("Bu e-posta adresi zaten kullanılıyor");
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);
            user.setRole("USER"); // Varsayılan rol

            userService.saveUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            return "redirect:/home";
        }

        // Ensure firstName and lastName are not null
        if (user.getFirstName() == null) user.setFirstName("");
        if (user.getLastName() == null) user.setLastName("");
        if (user.getName() == null) user.setName("");

        model.addAttribute("user", user);
        model.addAttribute("assignedTasks", taskService.getTasksForUser(user));
        model.addAttribute("createdTasks", taskService.getTasksCreatedByUser(user));
        model.addAttribute("rooms", roomService.getRoomsForUser(user));

        return "user-profile";
    }

    @PostMapping("/{userId}/bio")
    @ResponseBody
    public String updateBio(@PathVariable Long userId, @RequestBody String bio) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return "Kullanıcı bulunamadı";
            }

            user.setBio(bio);
            userService.save(user);
            return "Biyografi güncellendi";
        } catch (Exception e) {
            return "Biyografi güncellenirken bir hata oluştu: " + e.getMessage();
        }
    }

    @PostMapping("/{userId}/update")
    @ResponseBody
    public java.util.Map<String, Object> updateProfile(@PathVariable Long userId, @RequestBody java.util.Map<String, String> payload, HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            response.put("success", false);
            response.put("message", "Bu işlem için yetkiniz yok.");
            return response;
        }
        try {
            User user = userService.findById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Kullanıcı bulunamadı");
                return response;
            }
            String firstName = payload.get("firstName");
            String lastName = payload.get("lastName");
            String email = payload.get("email");
            String bio = payload.get("bio");
            
            if (firstName != null && !firstName.isBlank()) {
                user.setFirstName(firstName);
            }
            if (lastName != null && !lastName.isBlank()) {
                user.setLastName(lastName);
            }
            if (email != null && !email.isBlank()) {
                user.setEmail(email);
            }
            user.setBio(bio);
            userService.save(user);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
