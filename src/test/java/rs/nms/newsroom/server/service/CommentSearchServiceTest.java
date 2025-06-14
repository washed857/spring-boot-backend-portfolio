package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.*;
import rs.nms.newsroom.server.dto.CommentDTOs;
import rs.nms.newsroom.server.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentSearchServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentSearchService commentSearchService;

    @Test
    void testSearch_byTextAndStoryId_result() {
        Comment comment = new Comment();
        comment.setId(6L);
        User user = new User();
        user.setId(2L);
        user.setFullName("Mark");
        comment.setUser(user);
        comment.setCommentText("Short test");
        Story story = new Story();
        story.setId(1L);
        comment.setStory(story);
        comment.setCreatedAt(LocalDateTime.now());

        when(commentRepository.searchByTextAndStoryId("test", 1L)).thenReturn(List.of(comment));

        List<CommentDTOs.CommentResponse> result = commentSearchService.search("test", 1L);

        assertEquals(1, result.size());
        assertEquals(6L, result.get(0).getId());
        assertEquals(2L, result.get(0).getUserId());
        assertEquals("Short test", result.get(0).getCommentText());
    }

    @Test
    void testSearch_byTextAndStoryId_empty() {
        when(commentRepository.searchByTextAndStoryId("none", 22L)).thenReturn(List.of());
        List<CommentDTOs.CommentResponse> result = commentSearchService.search("none", 22L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearch_nullQuery_nullStoryId() {
        when(commentRepository.searchByTextAndStoryId(null, null)).thenReturn(List.of());
        List<CommentDTOs.CommentResponse> result = commentSearchService.search(null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearch_onlyText() {
        Comment comment = new Comment();
        comment.setId(3L);
        User user = new User();
        user.setId(5L);
        user.setFullName("Somebody");
        comment.setUser(user);
        comment.setCommentText("Just text");

        when(commentRepository.searchByTextAndStoryId("Just", null)).thenReturn(List.of(comment));
        List<CommentDTOs.CommentResponse> result = commentSearchService.search("Just", null);

        assertEquals(1, result.size());
        assertEquals("Just text", result.get(0).getCommentText());
        assertEquals(5L, result.get(0).getUserId());
    }
}