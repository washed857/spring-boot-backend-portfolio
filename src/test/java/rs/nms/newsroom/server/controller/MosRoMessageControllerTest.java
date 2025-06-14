package rs.nms.newsroom.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.config.security.JwtAuthenticationFilter;
import rs.nms.newsroom.server.config.security.JwtTokenUtil;
import rs.nms.newsroom.server.dto.MosRoMessageDTOs;
import rs.nms.newsroom.server.service.MosRoMessageService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = MosRoMessageController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = { JwtAuthenticationFilter.class, JwtTokenUtil.class }
    )
)
@AutoConfigureMockMvc(addFilters = false)
class MosRoMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MosRoMessageService mosRoMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MosRoMessageDTOs.MosRoMessageResponse sampleResponse() {
        MosRoMessageDTOs.MosRoMessageResponse dto = new MosRoMessageDTOs.MosRoMessageResponse();
        dto.setId(1L);
        dto.setRoId("RO-001");
        dto.setSlug("breaking-news");
        dto.setMeta("<meta>value</meta>");
        dto.setReceivedAt(LocalDateTime.of(2025, 6, 12, 15, 0));
        return dto;
    }

    @Test
    void getAll_shouldReturnListOfRoMessages() throws Exception {
        List<MosRoMessageDTOs.MosRoMessageResponse> responseList = List.of(sampleResponse());

        when(mosRoMessageService.getAll()).thenReturn(responseList);

        mockMvc.perform(get("/api/mos/ro"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].roId").value("RO-001"))
                .andExpect(jsonPath("$[0].slug").value("breaking-news"))
                .andExpect(jsonPath("$[0].meta").value("<meta>value</meta>"))
                .andExpect(jsonPath("$[0].receivedAt").value("2025-06-12T15:00:00"));
    }
}