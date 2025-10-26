package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Override
    @Transactional
    public Room createRoom(String name, User createdBy) {
        Room room = new Room();
        room.setName(name);
        room.setCode(generateUniqueCode());
        room.setCreatedBy(createdBy);
        room.setMembers(new ArrayList<>());
        room.getMembers().add(createdBy); // Oluşturan kişiyi otomatik olarak üye yap
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public Room joinRoom(String code, User user) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + code));
        
        if (room.getMembers().contains(user)) {
            throw new RuntimeException("Zaten bu odanın üyesisiniz");
        }
        
        room.getMembers().add(user);
        return roomRepository.save(room);
    }

    @Override
    public List<Room> getRoomsForUser(User user) {
        return roomRepository.findByMembersContaining(user);
    }

    @Override
    public List<Room> getRoomsCreatedByUser(User user) {
        return roomRepository.findByCreatedBy(user);
    }

    @Override
    public Room getRoomByCode(String code) {
        return roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + code));
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + roomId));
        
        if (room.getCreatedBy().equals(user)) {
            throw new RuntimeException("Oda sahibi odadan ayrılamaz");
        }
        
        room.getMembers().remove(user);
        roomRepository.save(room);
    }

    @Override
    public List<User> getRoomMembers(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));
        return room.getMembers();
    }

    @Override
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı"));
    }

    @Override
    public Room saveRoom(Room room) {
        try {
            logger.info("Oda kaydediliyor: {}", room.getName());
            Room savedRoom = roomRepository.save(room);
            logger.info("Oda başarıyla kaydedildi: {}", savedRoom.getName());
            return savedRoom;
        } catch (Exception e) {
            logger.error("Oda kaydedilirken hata oluştu", e);
            throw e;
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (roomRepository.existsByCode(code));
        return code;
    }
} 