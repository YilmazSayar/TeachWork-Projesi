package TeachWork.example.TeachWork.service;

import TeachWork.example.TeachWork.model.Group;
import TeachWork.example.TeachWork.model.User;
import TeachWork.example.TeachWork.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class GroupService {
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private GroupRepository groupRepository;

    @Transactional
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    @Transactional
    public void addUserToGroup(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grup bulunamadı"));
        group.getMembers().add(user);
        groupRepository.save(group);
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, User user) {
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Grup bulunamadı"));
            
            // Kullanıcının grupta olup olmadığını kontrol et
            if (!group.getMembers().contains(user)) {
                throw new RuntimeException("Kullanıcı bu grupta değil");
            }
            
            // Grubu oluşturan kişi gruptan çıkamaz
            if (group.getCreatedBy().getId().equals(user.getId())) {
                throw new RuntimeException("Grup oluşturan kişi gruptan çıkamaz");
            }
            
            group.getMembers().remove(user);
            groupRepository.save(group);
            logger.info("Kullanıcı {} gruptan çıkarıldı: {}", user.getEmail(), group.getName());
        } catch (Exception e) {
            logger.error("Gruptan çıkış sırasında hata: {}", e.getMessage());
            throw e;
        }
    }

    public List<Group> getGroupsByUser(User user) {
        return groupRepository.findByMembersContaining(user);
    }
} 