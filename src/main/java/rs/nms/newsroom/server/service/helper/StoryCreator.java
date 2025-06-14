package rs.nms.newsroom.server.service.helper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rs.nms.newsroom.server.domain.*;
import rs.nms.newsroom.server.domain.enums.StoryStatus;
import rs.nms.newsroom.server.dto.StoryDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.*;
import rs.nms.newsroom.server.service.StoryLogService;
import rs.nms.newsroom.server.util.ClientContextHelper;

import java.util.UUID;

import static rs.nms.newsroom.server.domain.StoryLog.OperationType;

@Component
@RequiredArgsConstructor
public class StoryCreator {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final RundownRepository rundownRepository;
    private final StoryTypeRepository storyTypeRepository;
    private final StoryLogService storyLogService;
    private final StoryMapper storyMapper;

    @Transactional
    public StoryDTOs.StoryResponse create(StoryDTOs.StoryCreateRequest createRequest) {
        User author = userRepository.findById(createRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createRequest.getAuthorId()));

        Story story = new Story();
        story.setTitle(createRequest.getTitle());

        if (createRequest.getStatus() != null && !createRequest.getStatus().isBlank()) {
            try {
                story.setStatus(StoryStatus.valueOf(createRequest.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid story status: " + createRequest.getStatus());
            }
        } else {
            story.setStatus(StoryStatus.DRAFT);
        }

        story.setAuthor(author);

        // Rundown je opciono
        if (createRequest.getRundownId() != null) {
            Rundown rundown = rundownRepository.findById(createRequest.getRundownId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rundown not found with id: " + createRequest.getRundownId()));
            story.setRundown(rundown);
        }

        // External ID
        story.setExternalId(createRequest.getExternalId() != null ? createRequest.getExternalId() : "S-" + UUID.randomUUID());

        // Client id (pretpostavka da helper nikad ne vraća null)
        story.setClientId(ClientContextHelper.getCurrentClientId());

        // Story type je opciono
        if (createRequest.getStoryTypeId() != null) {
            StoryType storyType = storyTypeRepository.findById(createRequest.getStoryTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("StoryType not found with id: " + createRequest.getStoryTypeId()));
            story.setStoryType(storyType);
        }

        Story saved = storyRepository.save(story);
        storyLogService.logChange(saved, OperationType.CREATE, null);
        return storyMapper.mapToResponse(saved);
    }
}
