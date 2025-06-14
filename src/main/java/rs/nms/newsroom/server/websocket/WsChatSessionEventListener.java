package rs.nms.newsroom.server.websocket;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WsChatSessionEventListener {

    private final ChatPresenceTracker presenceTracker;
    private final SimpMessageSendingOperations messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(WsChatSessionEventListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        logger.info("WebSocket connected: {}", event.getMessage().getHeaders());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (roomId != null && userId != null) {
            presenceTracker.removeUserFromRoom(roomId, userId);

            messagingTemplate.convertAndSend(
                    "/topic/chat.room." + roomId + "/presence",
                    presenceTracker.getUsersInRoom(roomId)
            );

            messagingTemplate.convertAndSend(
                    "/topic/chat.room." + roomId + "/events",
                    String.format("User %s left the room.", username != null ? username : ("#" + userId))
            );

            logger.info("User {} disconnected from room {}", userId, roomId);
        } else {
            logger.debug("Session disconnect without room or user id");
        }
    }
}