package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.service.SignatureExportService;

import java.io.File;

/**
 * REST controller for exporting CG signature data as XML files.
 * <p>
 * Provides endpoints for generating and downloading signature metadata 
 * from StoryItems for a given Rundown.
 * </p>
 */
@RestController
@RequestMapping("/signature-export")
@RequiredArgsConstructor
@Tag(name = "Signature Export", description = "Export CG signature data from StoryItems")
public class SignatureExportController {

    private final SignatureExportService signatureExportService;

    @Operation(
        summary = "Export CG signatures to XML",
        description = "Generates an XML file containing CG name/type/comment fields for a given Rundown"
    )
    @GetMapping("/rundown/{rundownId}")
    public ResponseEntity<FileSystemResource> exportSignatures(@PathVariable Long rundownId) {
        File file = signatureExportService.exportSignaturesToXml(rundownId);
        FileSystemResource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_XML)
                .body(resource);
    }
}