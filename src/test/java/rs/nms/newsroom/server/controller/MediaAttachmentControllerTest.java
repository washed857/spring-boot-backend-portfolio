package rs.nms.newsroom.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import rs.nms.newsroom.server.config.security.JwtAuthenticationFilter;
import rs.nms.newsroom.server.config.security.JwtTokenUtil;
import rs.nms.newsroom.server.dto.MediaAttachmentDTOs;
import rs.nms.newsroom.server.dto.MediaAttachmentSearchCriteria;
import rs.nms.newsroom.server.service.MediaAttachmentService;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	    controllers = MediaAttachmentController.class,
	    excludeFilters = @ComponentScan.Filter(
	        type = FilterType.ASSIGNABLE_TYPE,
	        classes = { JwtAuthenticationFilter.class, JwtTokenUtil.class }
	)
)
@AutoConfigureMockMvc(addFilters = false)
class MediaAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaAttachmentService mediaAttachmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private MediaAttachmentDTOs.MediaAttachmentResponse buildSampleResponse() {
        MediaAttachmentDTOs.MediaAttachmentResponse resp = new MediaAttachmentDTOs.MediaAttachmentResponse();
        resp.setId(1L);
        resp.setFileName("test.jpg");
        resp.setFilePath("/uploads/test.jpg");
        resp.setMediaType("IMAGE");
        resp.setFormat("jpg");
        resp.setDurationSec(null);
        resp.setWidth(1920);
        resp.setHeight(1080);
        resp.setUploadedBy("uros");
        resp.setUploadedAt(LocalDateTime.now());
        return resp;
    }

    @Test
    @WithMockUser(authorities = "MEDIA_UPLOAD")
    void uploadMedia_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        MediaAttachmentDTOs.MediaAttachmentResponse response = buildSampleResponse();

        when(mediaAttachmentService.uploadMedia(
                ArgumentMatchers.any(MockMultipartFile.class),
                ArgumentMatchers.any(MediaAttachmentDTOs.MediaAttachmentUploadRequest.class),
                ArgumentMatchers.anyString())
        ).thenReturn(response);

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("fileName", "test.jpg")
                        .param("mediaType", "IMAGE")
                        .param("format", "jpg")
                        .with(request -> { request.setMethod("POST"); return request; })
                        .principal(() -> "uros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.jpg"))
                .andExpect(jsonPath("$.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.uploadedBy").value("uros"));
    }


    @Test
    @WithMockUser(authorities = "MEDIA_VIEW")
    void getById_success() throws Exception {
        MediaAttachmentDTOs.MediaAttachmentResponse response = buildSampleResponse();
        when(mediaAttachmentService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/media/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.jpg"));
    }

    @Test
    @WithMockUser(authorities = "MEDIA_VIEW")
    void getByMediaType_success() throws Exception {
        MediaAttachmentDTOs.MediaAttachmentResponse response = buildSampleResponse();
        when(mediaAttachmentService.getByMediaType("IMAGE")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/media/by-type/IMAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("test.jpg"));
    }

    @Test
    @WithMockUser(authorities = "MEDIA_VIEW")
    void searchMediaAttachments_success() throws Exception {
        MediaAttachmentDTOs.MediaAttachmentResponse response = buildSampleResponse();
        when(mediaAttachmentService.search(
                ArgumentMatchers.any(MediaAttachmentSearchCriteria.class),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(response)));

        MediaAttachmentSearchCriteria criteria = new MediaAttachmentSearchCriteria();
        criteria.setQuery("test");

        mockMvc.perform(post("/api/media/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fileName").value("test.jpg"));
    }

    @Test
    @WithMockUser(authorities = "MEDIA_DELETE")
    void delete_success() throws Exception {
        doNothing().when(mediaAttachmentService).delete(1L);

        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isNoContent());
    }
}