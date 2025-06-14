package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.nms.newsroom.server.dto.MosRoMessageDTOs;
import rs.nms.newsroom.server.service.MosRoMessageService;

import java.util.List;

/**
 * REST controller for retrieving MOS Rundown (ro) messages.
 */
@RestController
@RequestMapping("/mos/ro")
@RequiredArgsConstructor
@Tag(
    name = "MOS Rundown (ro) Messages",
    description = "View and analyze all MOS Rundown (ro) messages received via the TCP/MOS protocol."
)
public class MosRoMessageController {

    private final MosRoMessageService mosRoMessageService;

    /**
     * Returns a list of all received MOS Rundown (ro) messages.
     *
     * @return list of MOS Rundown messages (DTO)
     */
    @GetMapping
    @Operation(
        summary = "Retrieve all MOS Rundown (ro) messages",
        description = "Returns all received MOS Rundown (ro) messages in the system for audit and integration monitoring purposes."
    )
    public List<MosRoMessageDTOs.MosRoMessageResponse> getAll() {
        return mosRoMessageService.getAll();
    }
}
