package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.dto.UserLogDTOs;
import rs.nms.newsroom.server.service.UserLogService;

import java.util.List;

@Tag(
	 name = "User Audit Log",
	 description = "Overview and analysis of all changes and activities related to user accounts. Access is restricted to administrators only. Every user creation, update, deletion, or status change is audit-logged and can be reviewed using this endpoint."
)
@RestController
@RequestMapping("/user-logs")
@RequiredArgsConstructor
public class UserLogController {

    private final UserLogService userLogService;

    @Operation(
    	    summary = "Get All User Audit Logs",
    	    description = """
    	        Returns a complete list of all user audit logs (actions: creation, update, deletion, status and password changes).
    	        Only users with ADMIN privileges have access to this endpoint.
    	        Contains a before and after JSON snapshot for each change, allowing detailed analysis of all modifications.
    	    """
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserLogDTOs.UserLogResponse>> getAll() {
        return ResponseEntity.ok(userLogService.getAll());
    }
}