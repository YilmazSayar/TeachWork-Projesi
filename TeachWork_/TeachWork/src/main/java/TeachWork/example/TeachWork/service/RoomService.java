package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Room;
import TeachWork.example.TeachWork.model.User;
import java.util.List;

public interface RoomService {
    Room createRoom(String name, User creator);
    Room joinRoom(String code, User user);
    List<Room> getRoomsForUser(User user);
    List<Room> getRoomsCreatedByUser(User user);
    Room getRoomByCode(String code);
    void leaveRoom(Long roomId, User user);
    List<User> getRoomMembers(Long roomId);
    Room getRoomById(Long id);
    Room saveRoom(Room room);
} 