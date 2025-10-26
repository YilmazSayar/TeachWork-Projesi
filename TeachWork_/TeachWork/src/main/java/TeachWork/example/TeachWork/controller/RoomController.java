package TeachWork.example.TeachWork.controller;

import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.model.Task;
import TeachWork.example.TeachWork.service.RoomService;
import TeachWork.example.TeachWork.service.UserService;
import TeachWork.example.TeachWork.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rooms")
public class RoomController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @GetMapping
    public String getRoomsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        
        // Sadece kullanıcının üyesi olduğu odaları getir
        List<Room> rooms = roomService.getRoomsForUser(currentUser).stream()
            .filter(room -> room.getMembers().contains(currentUser))
            .collect(Collectors.toList());
        
        // Her oda için görev sayısını hesapla
        Map<Long, Long> roomTaskCounts = rooms.stream()
            .collect(Collectors.toMap(
                Room::getId,
                room -> taskService.getTasksByRoomId(room.getId()).stream()
                    .filter(task -> task.getAssignedTo().equals(currentUser))
                    .count()
            ));
        
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTaskCounts", roomTaskCounts);
        return "rooms";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            String roomName = request.get("name");
            if (roomName == null || roomName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Oda adı boş olamaz");
            }

            // Yeni oda oluştur
            Room room = new Room();
            room.setName(roomName.trim());
            room.setCode(generateRoomCode());
            room.setCreatedBy(currentUser);
            
            // Sadece oluşturan kişiyi üye olarak ekle
            List<User> members = new ArrayList<>();
            members.add(currentUser);
            room.setMembers(members);

            // Odayı kaydet
            room = roomService.saveRoom(room);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    @ResponseBody
    public ResponseEntity<?> joinRoom(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Oda kodu boş olamaz");
            }

            Room room = roomService.joinRoom(code.trim().toUpperCase(), currentUser);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-rooms")
    @ResponseBody
    public ResponseEntity<List<Room>> getMyRooms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        
        // Sadece kullanıcının üyesi olduğu odaları getir
        List<Room> rooms = roomService.getRoomsForUser(currentUser).stream()
            .filter(room -> room.getMembers().contains(currentUser))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/created-rooms")
    @ResponseBody
    public ResponseEntity<List<Room>> getCreatedRooms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName());
        
        List<Room> rooms = roomService.getRoomsCreatedByUser(currentUser);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}/members")
    @ResponseBody
    public ResponseEntity<?> getRoomMembers(@PathVariable Long roomId) {
        try {
            List<User> members = roomService.getRoomMembers(roomId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{roomId}/tasks")
    @ResponseBody
    public ResponseEntity<?> getRoomTasks(@PathVariable Long roomId) {
        try {
            logger.info("Getting tasks for room ID: {}", roomId);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            logger.info("Current user: {}", currentUser.getEmail());
            
            // Kullanıcının o odada olup olmadığını kontrol et
            Room room = roomService.getRoomById(roomId);
            logger.info("Found room: {}", room.getName());
            
            if (!room.getMembers().contains(currentUser)) {
                logger.warn("User {} is not a member of room {}", currentUser.getEmail(), roomId);
                return ResponseEntity.badRequest().body("Bu odaya erişim izniniz yok.");
            }
            
            // Odadaki görevleri getir
            List<Task> roomTasks = taskService.getTasksByRoomId(roomId);
            logger.info("Found {} tasks for room {}", roomTasks.size(), roomId);
            
            return ResponseEntity.ok(roomTasks);
            
        } catch (Exception e) {
            logger.error("Error getting tasks for room {}: {}", roomId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Görevler yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/{roomId}/leave")
    @ResponseBody
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName());
            
            roomService.leaveRoom(roomId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 