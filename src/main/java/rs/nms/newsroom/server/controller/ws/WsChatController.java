package rs.nms.newsroom.server.controller.ws;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import rs.nms.newsroom.server.dto.ChatDTOs;
import rs.nms.newsroom.server.service.ChatService;
import rs.nms.newsroom.server.websocket.ChatPresenceTracker;

import java.util.Objects;

/**
 * Handles real-time WebSocket chat communication for newsroom users.
 * <p>
 * This controller manages user presence, message delivery, and typing events 
 * within chat rooms, providing seamless collaboration in a modern newsroom environment.
 * </p>
 * 
 */
@Controller
@RequiredArgsConstructor
@Validated
public class WsChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final ChatPresenceTracker presenceTracker;

    private static final Logger logger = LoggerFactory.getLogger(WsChatController.class);

    /**
     * Handles sending a new chat message to a chat room.
     * The message is broadcast to all users subscribed to the specified room.
     *
     * @param messageRequest The chat message request payload (validated).
     * @param headerAccessor WebSocket session header accessor.
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Valid @Payload ChatDTOs.SendMessageRequest messageRequest,
            SimpMessageHeaderAccessor headerAccessor) {

        if (headerAccessor.getSessionAttributes() == null) {
            logger.warn("Session attributes missing");
            return;
        }

        Long senderId = Long.valueOf(
                Objects.toString(headerAccessor.getSessionAttributes().get("userId"), "0"));

        Long roomId = messageRequest.getRoomId();
        if (roomId == null || senderId == 0) {
            logger.warn("Invalid message: roomId={}, senderId={}", roomId, senderId);
            return;
        }

        headerAccessor.getSessionAttributes().put("roomId", roomId);

        ChatDTOs.ChatMessageResponse saved = chatService.sendMessage(messageRequest, senderId);

        // Broadcast message to all clients subscribed to this room
        messagingTemplate.convertAndSend(
                "/topic/chat.room." + roomId,
                saved);
    }

    /**
     * Handles a new user joining a chat room.
     * Tracks user presence and notifies other users in the room.
     *
     * @param joinMessage   The message containing user and room details.
     * @param headerAccessor WebSocket session header accessor.
     */
    @MessageMapping("/chat.addUser")
    public void addUser(
            @Payload ChatDTOs.ChatMessageResponse joinMessage,
            SimpMessageHeaderAccessor headerAccessor) {

        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", joinMessage.getSenderName());
            headerAccessor.getSessionAttributes().put("userId", joinMessage.getSenderId());
            headerAccessor.getSessionAttributes().put("roomId", joinMessage.getRoomId());

            presenceTracker.addUserToRoom(joinMessage.getRoomId(), joinMessage.getSenderId());

            // Notify all users in the room about the updated presence
            messagingTemplate.convertAndSend(
                    "/topic/chat.room." + joinMessage.getRoomId() + "/presence",
                    presenceTracker.getUsersInRoom(joinMessage.getRoomId()));

            // Broadcast join event
            messagingTemplate.convertAndSend(
                    "/topic/chat.room." + joinMessage.getRoomId() + "/events",
                    String.format("User %s joined the room.", joinMessage.getSenderName()));
        }
    }

    /**
     * Handles typing indicator events.
     * Notifies all users in the room when a user is typing.
     *
     * @param typingEvent The typing event payload.
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatDTOs.TypingEvent typingEvent) {
        if (typingEvent.getRoomId() != null && typingEvent.getUserId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/chat.room." + typingEvent.getRoomId() + "/typing",
                    typingEvent);
        } else {
            logger.warn("Invalid typingEvent: {}", typingEvent);
        }
    }
}