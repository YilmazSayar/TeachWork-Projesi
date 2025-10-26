package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.User;
import java.util.List;

public interface UserService {
    User findByEmail(String email);
    User findById(Long id);
    User save(User user);
    void delete(Long id);
    List<User> findAll();
    User saveUser(User user);
    boolean existsByEmail(String email);
    User getUserById(Long id);
    List<User> getAllUsers();
}
