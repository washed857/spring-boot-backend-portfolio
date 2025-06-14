package rs.nms.newsroom.server.service.mos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.nms.newsroom.server.domain.Rundown;
import rs.nms.newsroom.server.domain.Story;
import rs.nms.newsroom.server.dto.mos.RoStoryInsertMessage;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.RundownRepository;
import rs.nms.newsroom.server.repository.StoryRepository;
import rs.nms.newsroom.server.websocket.MosRoWebSocketBroadcaster;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MosStoryInsertHandler {

    private final RundownRepository rundownRepository;
    private final StoryRepository storyRepository;
    private final MosRoWebSocketBroadcaster webSocketBroadcaster;

    public void handle(RoStoryInsertMessage message) {
        log.info("Handling roStoryInsert: RO ID: {}, Story ID: {}, Slug: {}",
                message.getRoID(), message.getStoryID(), message.getStorySlug());

        Rundown rundown = rundownRepository.findByExternalId(message.getRoID())
                .orElseThrow(() -> new ResourceNotFoundException("Rundown not found for externalId: " + message.getRoID()));

        Story story = storyRepository.findByExternalId(message.getStoryID())
                .orElseGet(() -> {
                    Story newStory = new Story();
                    newStory.setExternalId(message.getStoryID());
                    newStory.setTitle(message.getStorySlug());
                    newStory.setRundown(rundown);
                    newStory.setCreatedAt(LocalDateTime.now());
                    newStory.setClientId(rundown.getClientId());
                    log.info("Created new Story with externalId: {}", message.getStoryID());
                    return storyRepository.save(newStory);
                });

        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "STORY_INSERT");
        payload.put("roId", message.getRoID());
        payload.put("storyId", message.getStoryID());
        payload.put("slug", message.getStorySlug());
        payload.put("rundownId", rundown.getId());
        payload.put("storyDbId", story.getId());

        webSocketBroadcaster.broadcastGeneric("/topic/mos/story", payload);
    }
}