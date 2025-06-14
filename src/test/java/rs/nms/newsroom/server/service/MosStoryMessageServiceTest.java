package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import rs.nms.newsroom.server.dto.mos.*;

import rs.nms.newsroom.server.service.mos.*;

import static org.mockito.Mockito.*;

class MosStoryMessageServiceTest {

    @Mock MosStoryInsertHandler insertHandler;
    @Mock MosStoryReplaceHandler replaceHandler;
    @Mock MosStoryMoveHandler moveHandler;
    @Mock MosStorySwapHandler swapHandler;
    @Mock MosStoryStatusHandler statusHandler;
    @Mock MosStoryDeleteHandler deleteHandler;

    @InjectMocks
    MosStoryMessageService mosStoryMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processRoStoryInsert_shouldDelegateToHandler() {
        RoStoryInsertMessage msg = new RoStoryInsertMessage();
        mosStoryMessageService.processRoStoryInsert(msg);
        verify(insertHandler).handle(msg);
    }

    @Test
    void processRoStoryReplace_shouldDelegateToHandler() {
        RoStoryReplaceMessage msg = new RoStoryReplaceMessage();
        mosStoryMessageService.processRoStoryReplace(msg);
        verify(replaceHandler).handle(msg);
    }

    @Test
    void processRoStoryMove_shouldDelegateToHandler() {
        RoStoryMoveMessage msg = new RoStoryMoveMessage();
        mosStoryMessageService.processRoStoryMove(msg);
        verify(moveHandler).handle(msg);
    }

    @Test
    void processRoStorySwap_shouldDelegateToHandler() {
        RoStorySwapMessage msg = new RoStorySwapMessage();
        mosStoryMessageService.processRoStorySwap(msg);
        verify(swapHandler).handle(msg);
    }

    @Test
    void processRoStoryStatus_shouldDelegateToHandler() {
        RoStoryStatusMessage msg = new RoStoryStatusMessage();
        mosStoryMessageService.processRoStoryStatus(msg);
        verify(statusHandler).handle(msg);
    }

    @Test
    void processRoStoryDelete_shouldDelegateToHandler() {
        RoStoryDeleteMessage msg = new RoStoryDeleteMessage();
        mosStoryMessageService.processRoStoryDelete(msg);
        verify(deleteHandler).handle(msg);
    }
}
