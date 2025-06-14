package rs.nms.newsroom.server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.service.helper.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryCreator storyCreator;
    private final StoryUpdater storyUpdater;
    private final StoryStatusChanger storyStatusChanger;
    private final StoryMapper storyMapper;
    private final StoryDeleter storyDeleter;
    private final StoryLocker storyLocker;
    private final StoryVersionService storyVersionService;
    private final StoryRepository storyRepository;

    @Transactional
    public StoryDTOs.StoryResponse create(StoryDTOs.StoryCreateRequest request) {
        return storyCreator.create(request);
    }

    public StoryDTOs.StoryResponse getById(Long id) {
        return storyMapper.getById(id);
    }

    public List<StoryDTOs.StoryResponse> getByRundownId(Long rundownId) {
        return storyMapper.getByRundownId(rundownId);
    }

    public List<StoryDTOs.StoryResponse> getByAuthorId(Long authorId) {
        return storyMapper.getByAuthorId(authorId);
    }

    @Transactional
    public StoryDTOs.StoryResponse update(Long id, StoryDTOs.StoryUpdateRequest request) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));

        // Save version snapshot before update
        storyVersionService.createVersionSnapshot(story.getId(), story.getAuthor().getId());

        // Then perform the update
        return storyUpdater.update(id, request);
    }

    @Transactional
    public StoryDTOs.StoryResponse publish(Long id, String approvedBy) {
        return storyStatusChanger.publish(id, approvedBy);
    }

    @Transactional
    public StoryDTOs.StoryResponse reject(Long id, String rejectedBy) {
        return storyStatusChanger.reject(id, rejectedBy);
    }

    @Transactional
    public StoryDTOs.StoryResponse archive(Long id, String archivedBy) {
        return storyStatusChanger.archive(id, archivedBy);
    }

    @Transactional
    public StoryDTOs.StoryResponse lock(Long id) {
        return storyLocker.lockStory(id);
    }

    @Transactional
    public StoryDTOs.StoryResponse unlock(Long id) {
        return storyLocker.unlockStory(id);
    }

    @Transactional
    public void delete(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));

        // Save version snapshot before delete
        storyVersionService.createVersionSnapshot(story.getId(), story.getAuthor().getId());

        // Delegate deletion to helper
        storyDeleter.delete(id);
    }
}