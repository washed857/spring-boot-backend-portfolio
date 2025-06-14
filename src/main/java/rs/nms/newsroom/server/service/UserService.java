package rs.nms.newsroom.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import rs.nms.newsroom.server.config.storage.FileStorageUtil;
import rs.nms.newsroom.server.dto.UserDTOs;
import rs.nms.newsroom.server.dto.UserDTOs.UserLogEntry;
import rs.nms.newsroom.server.repository.UserLogRepository;
import rs.nms.newsroom.server.repository.UserRepository;
import rs.nms.newsroom.server.service.helper.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserLogRepository userLogRepository;
    private final UserCreator userCreator;
    private final UserUpdater userUpdater;
    private final UserMapper userMapper;
    private final UserSearcher userSearcher;
    private final UserStatusChanger userStatusChanger;
    private final UserPasswordChanger userPasswordChanger;
    private final FileStorageUtil fileStorageUtil;
    private final UserLogService userLogService;

    public UserDTOs.UserResponse create(UserDTOs.UserCreateRequest createRequest) {
        return userCreator.create(createRequest);
    }

    public UserDTOs.UserResponse getById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("User not found or deleted"));
    }

    public List<UserDTOs.UserResponse> getAll() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<UserDTOs.UserResponse> searchUsers(UserDTOs.UserSearchCriteria criteria, Pageable pageable) {
        return userSearcher.search(criteria, pageable);
    }

    public List<UserLogEntry> getUserLog(Long id) {
        return userLogRepository.findByTargetUserIdOrderByTimestampDesc(id).stream()
                .map(log -> {
                    UserLogEntry dto = new UserLogEntry();
                    dto.setTimestamp(log.getTimestamp());
                    dto.setOperation(log.getOperation().name());
                    dto.setPerformedBy("User ID: " + log.getActorUserId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTOs.UserResponse updateProfileImage(Long userId, MultipartFile file) throws IOException {
        var user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("User not found or deleted"));

        if (user.getProfileImagePath() != null && !user.getProfileImagePath().equals("/static/profile-default.png")) {
            try {
                fileStorageUtil.deleteFile(user.getProfileImagePath());
            } catch (IOException ignore) {}
        }

        String newImagePath = fileStorageUtil.storeFile(file);
        user.setProfileImagePath(newImagePath);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserDTOs.UserResponse update(Long id, UserDTOs.UserUpdateRequest updateRequest) {
        return userUpdater.update(id, updateRequest);
    }

    /**
     * Soft deletes a user (marks as deleted, does not physically remove from database).
     */
    @Transactional
    public void delete(Long id) {
        userRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .ifPresent(user -> {
                    user.setDeletedAt(LocalDateTime.now());
                    user.setStatus("DELETED");
                    userRepository.save(user);
                    userLogService.logChange(user, rs.nms.newsroom.server.domain.UserLog.OperationType.DELETE, null);
                });
    }

    public void changeStatus(Long id, String status) {
        userStatusChanger.changeStatus(id, status);
    }

    public void changePassword(Long id, UserDTOs.ChangePasswordRequest request) {
        userPasswordChanger.changePassword(id, request);
    }

    public UserDTOs.UserStatistics getUserStatistics() {
        var users = userRepository.findAll().stream().filter(u -> u.getDeletedAt() == null).toList();
        UserDTOs.UserStatistics stats = new UserDTOs.UserStatistics();
        stats.setTotal(users.size());
        stats.setActive(users.stream().filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus())).count());
        stats.setInactive(users.stream().filter(u -> !"ACTIVE".equalsIgnoreCase(u.getStatus())).count());
        stats.setAdmins(users.stream().filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName())).count());
        users.stream().map(u -> u.getCreatedAt()).max(Comparator.naturalOrder()).ifPresent(stats::setLastRegistered);
        return stats;
    }
}
