package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.*;
import rs.nms.newsroom.server.domain.enums.NotificationType;
import rs.nms.newsroom.server.dto.CommentDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private StoryRepository storyRepository;
    @Mock private StoryItemRepository storyItemRepository;
    @Mock private CommentLogService commentLogService;
    @Mock private NotificationSender notificationSender;

    @InjectMocks
    private CommentService commentService;

    private User mockUser;
    private Story mockStory;
    private StoryItem mockStoryItem;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("Test User");

        mockStory = new Story();
        mockStory.setId(100L);

        mockStoryItem = new StoryItem();
        mockStoryItem.setId(200L);
    }

    // ----------- CREATE COMMENT --------------

    @Test
    void testCreateComment_story_success() {
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("This is a comment.");
        req.setStoryId(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(100L)).thenReturn(Optional.of(mockStory));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        var result = commentService.createComment(req, 1L);

        assertNotNull(result);
        assertEquals("This is a comment.", result.getCommentText());
        assertEquals(1L, result.getUserId());
        assertEquals(100L, result.getStoryId());
        verify(notificationSender).sendGlobalNotification(eq(NotificationType.NEW_COMMENT), anyString(), any());
        verify(commentLogService).log(eq("CREATE"), isNull(), any(Comment.class));
    }

    @Test
    void testCreateComment_storyItem_success() {
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("Comment on story item.");
        req.setStoryItemId(200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storyItemRepository.findById(200L)).thenReturn(Optional.of(mockStoryItem));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(11L);
            return c;
        });

        var result = commentService.createComment(req, 1L);

        assertNotNull(result);
        assertEquals("Comment on story item.", result.getCommentText());
        assertEquals(1L, result.getUserId());
        assertEquals(200L, result.getStoryItemId());
    }

    @Test
    void testCreateComment_storyItem_parent_success() {
        Comment parent = new Comment();
        parent.setId(88L);
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("Reply");
        req.setStoryItemId(200L);
        req.setParentId(88L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storyItemRepository.findById(200L)).thenReturn(Optional.of(mockStoryItem));
        when(commentRepository.findById(88L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(12L);
            return c;
        });

        var result = commentService.createComment(req, 1L);

        assertNotNull(result);
        assertEquals("Reply", result.getCommentText());
        assertEquals(88L, result.getParentId());
        verify(notificationSender).sendGlobalNotification(any(), anyString(), any());
        verify(commentLogService).log(eq("CREATE"), isNull(), any(Comment.class));
    }

    @Test
    void testCreateComment_anchorId_and_full_links() {
        // Comment with anchorId and all links
        Comment parent = new Comment();
        parent.setId(55L);
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("All links");
        req.setStoryId(100L);
        req.setStoryItemId(200L);
        req.setAnchorId("abc");
        req.setParentId(55L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(100L)).thenReturn(Optional.of(mockStory));
        when(storyItemRepository.findById(200L)).thenReturn(Optional.of(mockStoryItem));
        when(commentRepository.findById(55L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(15L);
            return c;
        });

        var result = commentService.createComment(req, 1L);

        assertEquals("abc", result.getAnchorId());
        assertEquals(100L, result.getStoryId());
        assertEquals(200L, result.getStoryItemId());
        assertEquals(55L, result.getParentId());
    }

    @Test
    void testCreateComment_userNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("x");
        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(req, 123L));
    }

    @Test
    void testCreateComment_storyNotFound() {
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("X");
        req.setStoryId(111L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(111L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(req, 1L));
    }

    @Test
    void testCreateComment_storyItemNotFound() {
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("Y");
        req.setStoryItemId(222L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(storyItemRepository.findById(222L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(req, 1L));
    }

    @Test
    void testCreateComment_parentNotFound() {
        CommentDTOs.CommentCreateRequest req = new CommentDTOs.CommentCreateRequest();
        req.setCommentText("Z");
        req.setStoryItemId(222L);
        req.setParentId(123L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(storyItemRepository.findById(222L)).thenReturn(Optional.of(mockStoryItem));
        when(commentRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(req, 1L));
    }

    // ----------- DELETE COMMENT -------------

    @Test
    void testDeleteComment_success() {
        Comment comment = new Comment();
        comment.setId(99L);

        when(commentRepository.findById(99L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(99L);

        verify(commentLogService).log(eq("DELETE"), eq(comment), isNull());
        verify(commentRepository).delete(eq(comment));
    }

    @Test
    void testDeleteComment_notFound() {
        when(commentRepository.findById(77L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(77L));
    }

    // ---------- GET/THREADING -------------

    @Test
    void testGetCommentsByStoryId() {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setUser(mockUser);
        comment.setCommentText("test");
        comment.setStory(mockStory);

        when(commentRepository.findByStoryIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(comment));

        var result = commentService.getCommentsByStoryId(100L);

        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getCommentText());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void testGetCommentsByStoryItemId() {
        Comment comment = new Comment();
        comment.setId(7L);
        comment.setUser(mockUser);
        comment.setCommentText("item");
        comment.setStoryItem(mockStoryItem);

        when(commentRepository.findByStoryItemIdOrderByCreatedAtDesc(200L)).thenReturn(List.of(comment));

        var result = commentService.getCommentsByStoryItemId(200L);

        assertEquals(1, result.size());
        assertEquals("item", result.get(0).getCommentText());
        assertEquals(200L, result.get(0).getStoryItemId());
    }

    @Test
    void testGetThreadedCommentsByStoryItem() {
        Comment parent = new Comment();
        parent.setId(1L);
        parent.setUser(mockUser);
        parent.setCommentText("parent");
        parent.setStoryItem(mockStoryItem);

        Comment child = new Comment();
        child.setId(2L);
        child.setUser(mockUser);
        child.setCommentText("child");
        child.setParent(parent);
        child.setStoryItem(mockStoryItem);

        when(commentRepository.findByStoryItemIdOrderByCreatedAtDesc(200L)).thenReturn(List.of(parent, child));

        var result = commentService.getThreadedCommentsByStoryItem(200L);
        assertEquals(1, result.size()); // only parent at root level
        assertEquals("parent", result.get(0).getCommentText());
        assertNotNull(result.get(0).getReplies());
        assertEquals(1, result.get(0).getReplies().size());
        assertEquals("child", result.get(0).getReplies().get(0).getCommentText());
    }

}