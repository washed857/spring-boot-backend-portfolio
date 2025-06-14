package rs.nms.newsroom.server.service.helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.service.StoryLogService;
import rs.nms.newsroom.server.service.StoryVersionService;

import static rs.nms.newsroom.server.domain.StoryLog.OperationType;

@Component
@RequiredArgsConstructor
public class StoryDeleter {

    private final StoryRepository storyRepository;
    private final StoryLogService storyLogService;
    private final StoryUpdater storyUpdater;
    private final StoryVersionService storyVersionService;

    @Transactional
    public void delete(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + id));

        // Pre brisanja snimi snapshot verziju (pošalji ID i authorId)
        storyVersionService.createVersionSnapshot(story.getId(), story.getAuthor().getId());

        // Možeš i dalje da koristiš snapshot za log
        Story snapshot = storyUpdater.clone(story);

        storyLogService.logChange(story, OperationType.DELETE, snapshot);
        storyRepository.delete(story);
    }
}
