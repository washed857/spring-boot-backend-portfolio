package rs.nms.newsroom.server.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rs.nms.newsroom.server.service.SignatureExportService;

import java.io.File;
import java.io.FileWriter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SignatureExportController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = rs.nms.newsroom.server.config.security.JwtAuthenticationFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
class SignatureExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignatureExportService signatureExportService;

    @Test
    void exportSignatures_returnsXmlFile() throws Exception {
        File file = File.createTempFile("signature_export_test", ".xml");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><signatureExport></signatureExport>");
        }
        Mockito.when(signatureExportService.exportSignaturesToXml(eq(100L))).thenReturn(file);

        mockMvc.perform(get("/api/signature-export/rundown/100"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString(MediaType.APPLICATION_XML_VALUE)))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andExpect(header().longValue("Content-Length", file.length()))
                .andExpect(content().string(containsString("<signatureExport>")));
    }
}