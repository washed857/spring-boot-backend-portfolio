package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import rs.nms.newsroom.server.domain.ChatMessage;
import rs.nms.newsroom.server.domain.ChatRoom;
import rs.nms.newsroom.server.domain.User;
import rs.nms.newsroom.server.dto.ChatDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.exception.UnauthorizedException;
import rs.nms.newsroom.server.repository.ChatMessageRepository;
import rs.nms.newsroom.server.repository.ChatRoomRepository;
import rs.nms.newsroom.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ChatService chatService;

    private User sampleUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        return user;
    }

    private ChatRoom sampleRoom(Long id, String name, String type, User... users) {
        ChatRoom room = new ChatRoom();
        room.setId(id);
        room.setName(name);
        room.setType(type);
        room.setCreatedBy(users.length > 0 ? users[0].getUsername() : null);
        for (User u : users) {
            room.addParticipant(u);
        }
        room.setCreatedAt(LocalDateTime.now());
        return room;
    }

    private ChatMessage sampleMessage(Long id, ChatRoom room, User sender, String msg) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setRoom(room);
        message.setSender(sender);
        message.setMessage(msg);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    @Test
    void testCreateRoom_success() {
        User creator = sampleUser(1L, "user1");
        ChatDTOs.CreateRoomRequest req = new ChatDTOs.CreateRoomRequest();
        req.setName("Room");
        req.setType("PRIVATE");
        req.setParticipantIds(new Long[]{2L, 3L});

        User u2 = sampleUser(2L, "user2");
        User u3 = sampleUser(3L, "user3");

        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u2));
        when(userRepository.findById(3L)).thenReturn(Optional.of(u3));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> {
            ChatRoom r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        ChatDTOs.ChatRoomResponse resp = chatService.createRoom(req, 1L);

        assertEquals("Room", resp.getName());
        assertEquals("PRIVATE", resp.getType());
        assertEquals("user1", resp.getCreatedBy());
        assertEquals(99L, resp.getId());
    }

    @Test
    void testCreateRoom_userNotFound() {
        when(userRepository.findById(111L)).thenReturn(Optional.empty());
        ChatDTOs.CreateRoomRequest req = new ChatDTOs.CreateRoomRequest();
        assertThrows(ResourceNotFoundException.class, () -> chatService.createRoom(req, 111L));
    }

    @Test
    void testCreateRoom_participantNotFound() {
        User creator = sampleUser(1L, "creator");
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        ChatDTOs.CreateRoomRequest req = new ChatDTOs.CreateRoomRequest();
        req.setParticipantIds(new Long[]{42L});
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatService.createRoom(req, 1L));
    }

    @Test
    void testGetUserRooms() {
        User user = sampleUser(10L, "user");
        ChatRoom room1 = sampleRoom(1L, "R1", "PUBLIC", user);
        when(chatRoomRepository.findByParticipantId(10L)).thenReturn(List.of(room1));

        var list = chatService.getUserRooms(10L);
        assertEquals(1, list.size());
        assertEquals("R1", list.get(0).getName());
        assertEquals("PUBLIC", list.get(0).getType());
    }

    @Test
    void testSendMessage_success() {
        User sender = sampleUser(5L, "sender");
        ChatRoom room = sampleRoom(7L, "Chat", "PRIVATE", sender);

        ChatDTOs.SendMessageRequest req = new ChatDTOs.SendMessageRequest();
        req.setRoomId(7L);
        req.setMessage("Hello");

        when(chatRoomRepository.findById(7L)).thenReturn(Optional.of(room));
        when(userRepository.findById(5L)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(77L);
            m.setTimestamp(LocalDateTime.now());
            return m;
        });

        var resp = chatService.sendMessage(req, 5L);

        assertEquals(77L, resp.getId());
        assertEquals("Hello", resp.getMessage());
        assertEquals(7L, resp.getRoomId());
        assertEquals(5L, resp.getSenderId());
        assertEquals("sender", resp.getSenderName());
    }

    @Test
    void testSendMessage_roomNotFound() {
        ChatDTOs.SendMessageRequest req = new ChatDTOs.SendMessageRequest();
        req.setRoomId(1L);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatService.sendMessage(req, 5L));
    }

    @Test
    void testSendMessage_userNotFound() {
        ChatRoom room = sampleRoom(2L, "R", "PRIVATE");
        ChatDTOs.SendMessageRequest req = new ChatDTOs.SendMessageRequest();
        req.setRoomId(2L);
        when(chatRoomRepository.findById(2L)).thenReturn(Optional.of(room));
        when(userRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatService.sendMessage(req, 8L));
    }

    @Test
    void testSendMessage_notParticipant() {
        User sender = sampleUser(1L, "Sender");
        ChatRoom room = sampleRoom(3L, "R", "PRIVATE");
        ChatDTOs.SendMessageRequest req = new ChatDTOs.SendMessageRequest();
        req.setRoomId(3L);
        when(chatRoomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        assertThrows(UnauthorizedException.class, () -> chatService.sendMessage(req, 1L));
    }

    @Test
    void testGetRoomMessages_success() {
        User u = sampleUser(15L, "Member");
        ChatRoom room = sampleRoom(8L, "Room", "PRIVATE", u);

        ChatMessage msg = sampleMessage(1L, room, u, "message1");
        Pageable pageable = PageRequest.of(0, 10);

        when(chatRoomRepository.findById(8L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findByRoomIdOrderByTimestampDesc(8L, pageable))
                .thenReturn(new PageImpl<>(List.of(msg)));

        var page = chatService.getRoomMessages(8L, pageable, 15L);

        assertEquals(1, page.getTotalElements());
        var resp = page.getContent().get(0);
        assertEquals(1L, resp.getId());
        assertEquals("message1", resp.getMessage());
    }

    @Test
    void testGetRoomMessages_roomNotFound() {
        when(chatRoomRepository.findById(44L)).thenReturn(Optional.empty());
        Pageable pageable = PageRequest.of(0, 5);
        assertThrows(ResourceNotFoundException.class, () -> chatService.getRoomMessages(44L, pageable, 99L));
    }

    @Test
    void testGetRoomMessages_notParticipant() {
        User other = sampleUser(13L, "Other");
        ChatRoom room = sampleRoom(12L, "R", "PRIVATE");
        when(chatRoomRepository.findById(12L)).thenReturn(Optional.of(room));
        Pageable pageable = PageRequest.of(0, 3);
        assertThrows(UnauthorizedException.class, () -> chatService.getRoomMessages(12L, pageable, 13L));
    }
}