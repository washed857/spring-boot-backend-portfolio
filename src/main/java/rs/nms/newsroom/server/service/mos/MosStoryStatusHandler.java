package rs.nms.newsroom.server.service.mos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.nms.newsroom.server.domain.Rundown;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.domain.enums.StoryStatus;
import rs.nms.newsroom.server.dto.mos.RoStoryStatusMessage;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.RundownRepository;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.websocket.MosRoWebSocketBroadcaster;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MosStoryStatusHandler {

    private final RundownRepository rundownRepository;
    private final StoryRepository storyRepository;
    private final MosRoWebSocketBroadcaster webSocketBroadcaster;

    @Transactional
    public void handle(RoStoryStatusMessage message) {
        log.info("Handling roStoryStatus: RO ID: {}, Story ID: {}, Status: {}",
                message.getRoID(), message.getStoryID(), message.getStatus());

        Rundown rundown = rundownRepository.findByExternalId(message.getRoID())
                .orElseThrow(() -> new ResourceNotFoundException("Rundown not found for externalId: " + message.getRoID()));

        Story story = storyRepository.findByExternalId(message.getStoryID())
                .orElseThrow(() -> new ResourceNotFoundException("Story not found for externalId: " + message.getStoryID()));

        if (!story.getRundown().getId().equals(rundown.getId())) {
            throw new ResourceNotFoundException("Story does not belong to the given Rundown");
        }

        story.setStatus(
            Optional.ofNullable(message.getStatus())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return StoryStatus.valueOf(s);
                    } catch (Exception e) {
                        log.warn("Unknown status '{}', setting as null", s);
                        return null;
                    }
                })
                .orElse(null)
        );
        storyRepository.save(story);

        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "STORY_STATUS");
        payload.put("roId", message.getRoID());
        payload.put("storyId", message.getStoryID());
        payload.put("status", message.getStatus());
        payload.put("storyDbId", story.getId());

        webSocketBroadcaster.broadcastGeneric("/topic/mos/story", payload);
    }
}