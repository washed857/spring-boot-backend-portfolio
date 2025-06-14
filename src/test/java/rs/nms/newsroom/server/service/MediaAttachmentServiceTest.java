package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import rs.nms.newsroom.server.config.storage.FileStorageUtil;
import rs.nms.newsroom.server.domain.MediaAttachment;
import rs.nms.newsroom.server.dto.MediaAttachmentDTOs;
import rs.nms.newsroom.server.dto.MediaAttachmentSearchCriteria;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.MediaAttachmentRepository;
import rs.nms.newsroom.server.repository.spec.MediaAttachmentSpecifications;
import rs.nms.newsroom.server.util.MediaMetadataExtractor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaAttachmentServiceTest {

    @Mock
    private MediaAttachmentRepository mediaAttachmentRepository;
    @Mock
    private FileStorageUtil fileStorageUtil;
    @Mock
    private MediaAttachmentLogService mediaAttachmentLogService;

    @InjectMocks
    private MediaAttachmentService mediaAttachmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadMedia_shouldSaveAndReturnResponse() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        String filePath = "/media/uploads/file.mp4";
        when(fileStorageUtil.storeFile(file)).thenReturn(filePath);

        MediaAttachmentDTOs.MediaAttachmentUploadRequest request = new MediaAttachmentDTOs.MediaAttachmentUploadRequest();
        request.setFileName("test.mp4");
        request.setMediaType("VIDEO");
        request.setFormat("mp4");
        request.setDurationSec(120);

        MediaAttachment saved = new MediaAttachment();
        saved.setId(1L);
        saved.setFileName("test.mp4");
        saved.setFilePath(filePath);
        saved.setMediaType("VIDEO");
        saved.setFormat("mp4");
        saved.setDurationSec(120);
        saved.setUploadedBy("tester");
        saved.setUploadedAt(LocalDateTime.now());

        when(mediaAttachmentRepository.save(any(MediaAttachment.class))).thenReturn(saved);

        MediaAttachmentDTOs.MediaAttachmentResponse response = mediaAttachmentService
                .uploadMedia(file, request, "tester");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("test.mp4");
        assertThat(response.getFilePath()).isEqualTo(filePath);
        assertThat(response.getMediaType()).isEqualTo("VIDEO");
        assertThat(response.getFormat()).isEqualTo("mp4");
        assertThat(response.getUploadedBy()).isEqualTo("tester");

        verify(fileStorageUtil).storeFile(file);
        verify(mediaAttachmentRepository).save(any(MediaAttachment.class));
        verify(mediaAttachmentLogService).logOperation(saved, "UPLOAD", "tester");
    }

    @Test
    void uploadMedia_shouldExtractImageDimensionsWhenMissing() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        String filePath = "/media/uploads/image.jpg";
        when(fileStorageUtil.storeFile(file)).thenReturn(filePath);

        MediaAttachmentDTOs.MediaAttachmentUploadRequest request = new MediaAttachmentDTOs.MediaAttachmentUploadRequest();
        request.setFileName("image.jpg");
        request.setMediaType("IMAGE");
        request.setFormat("jpg");
        // Width/height null

        // Mock static method MediaMetadataExtractor.extractImageDimensions
        try (MockedStatic<MediaMetadataExtractor> mocked = mockStatic(MediaMetadataExtractor.class)) {
            mocked.when(() -> MediaMetadataExtractor.extractImageDimensions(file))
                    .thenReturn(new Integer[]{1920, 1080});

            MediaAttachment saved = new MediaAttachment();
            saved.setId(2L);
            saved.setFileName("image.jpg");
            saved.setFilePath(filePath);
            saved.setMediaType("IMAGE");
            saved.setFormat("jpg");
            saved.setWidth(1920);
            saved.setHeight(1080);
            saved.setUploadedBy("tester");
            saved.setUploadedAt(LocalDateTime.now());

            when(mediaAttachmentRepository.save(any(MediaAttachment.class))).thenReturn(saved);

            MediaAttachmentDTOs.MediaAttachmentResponse response = mediaAttachmentService
                    .uploadMedia(file, request, "tester");

            assertThat(response.getWidth()).isEqualTo(1920);
            assertThat(response.getHeight()).isEqualTo(1080);
        }
    }

    @Test
    void uploadMedia_shouldThrowWhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        MediaAttachmentDTOs.MediaAttachmentUploadRequest request = new MediaAttachmentDTOs.MediaAttachmentUploadRequest();
        request.setFileName("test.mp4");
        request.setMediaType("VIDEO");
        request.setFormat("mp4");

        assertThatThrownBy(() ->
                mediaAttachmentService.uploadMedia(file, request, "tester")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("File cannot be empty");

        verifyNoInteractions(fileStorageUtil);
        verifyNoInteractions(mediaAttachmentRepository);
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        MediaAttachment att = new MediaAttachment();
        att.setId(5L);
        att.setFileName("mydoc.pdf");
        att.setMediaType("DOCUMENT");
        att.setUploadedBy("tester");

        when(mediaAttachmentRepository.findById(5L)).thenReturn(Optional.of(att));

        MediaAttachmentDTOs.MediaAttachmentResponse resp = mediaAttachmentService.getById(5L);

        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getFileName()).isEqualTo("mydoc.pdf");
        assertThat(resp.getMediaType()).isEqualTo("DOCUMENT");
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(mediaAttachmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                mediaAttachmentService.getById(10L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByMediaType_shouldReturnList() {
        MediaAttachment att1 = new MediaAttachment();
        att1.setId(1L);
        att1.setFileName("a.mp4");
        att1.setMediaType("VIDEO");

        MediaAttachment att2 = new MediaAttachment();
        att2.setId(2L);
        att2.setFileName("b.mp4");
        att2.setMediaType("VIDEO");

        when(mediaAttachmentRepository.findByMediaType("VIDEO"))
                .thenReturn(List.of(att1, att2));

        List<MediaAttachmentDTOs.MediaAttachmentResponse> result = mediaAttachmentService.getByMediaType("VIDEO");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFileName()).isEqualTo("a.mp4");
        assertThat(result.get(1).getFileName()).isEqualTo("b.mp4");
    }

    @Test
    void search_shouldReturnPage() {
        MediaAttachment att = new MediaAttachment();
        att.setId(1L);
        att.setFileName("findme.jpg");
        att.setMediaType("IMAGE");

        MediaAttachmentSearchCriteria criteria = new MediaAttachmentSearchCriteria();
        criteria.setQuery("findme");
        criteria.setMediaType("IMAGE");

        Pageable pageable = PageRequest.of(0, 10);

        @SuppressWarnings("unchecked")
        Specification<MediaAttachment> spec = mock(Specification.class);

        // Mock static for specifications
        try (MockedStatic<MediaAttachmentSpecifications> specMock = mockStatic(MediaAttachmentSpecifications.class)) {
            specMock.when(() -> MediaAttachmentSpecifications.withFilters("findme", "IMAGE", null))
                    .thenReturn(spec);

            Page<MediaAttachment> page = new PageImpl<>(List.of(att), pageable, 1);
            when(mediaAttachmentRepository.findAll(spec, pageable)).thenReturn(page);

            Page<MediaAttachmentDTOs.MediaAttachmentResponse> result =
                    mediaAttachmentService.search(criteria, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getFileName()).isEqualTo("findme.jpg");
        }
    }

    @Test
    void delete_shouldRemoveMediaAndDeleteFile() throws IOException {
        MediaAttachment att = new MediaAttachment();
        att.setId(3L);
        att.setFilePath("/media/3.mp3");
        att.setUploadedBy("tester");

        when(mediaAttachmentRepository.findById(3L)).thenReturn(Optional.of(att));

        mediaAttachmentService.delete(3L);

        verify(fileStorageUtil).deleteFile("/media/3.mp3");
        verify(mediaAttachmentRepository).delete(att);
        verify(mediaAttachmentLogService).logOperation(att, "DELETE", "tester");
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(mediaAttachmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaAttachmentService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(fileStorageUtil);
    }

    @Test
    void delete_shouldThrowOnStorageException() throws IOException {
        MediaAttachment att = new MediaAttachment();
        att.setId(4L);
        att.setFilePath("/media/bad.mp4");
        att.setUploadedBy("tester");

        when(mediaAttachmentRepository.findById(4L)).thenReturn(Optional.of(att));
        doThrow(new IOException("fail")).when(fileStorageUtil).deleteFile("/media/bad.mp4");

        assertThatThrownBy(() -> mediaAttachmentService.delete(4L))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to delete media attachment with id: 4");
    }
}
