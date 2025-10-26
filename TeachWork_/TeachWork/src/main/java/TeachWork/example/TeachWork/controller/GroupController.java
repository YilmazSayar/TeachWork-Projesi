package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.model.Group;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.service.GroupService;
import TeachWork.example.TeachWork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/groups")
public class GroupController {
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listGroups(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        List<Group> userGroups = groupService.getGroupsByUser(currentUser);
        model.addAttribute("groups", userGroups);
        return "groups";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createGroup(@RequestBody Group group) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            group.setCreatedBy(currentUser);
            groupService.createGroup(group);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Grup oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/join")
    @ResponseBody
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            groupService.addUserToGroup(groupId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Gruba katılma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/leave")
    @ResponseBody
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            groupService.removeUserFromGroup(groupId, currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Gruptan başarıyla çıkıldı");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Gruptan çıkış hatası: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 