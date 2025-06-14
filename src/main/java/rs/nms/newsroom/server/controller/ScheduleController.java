package rs.nms.newsroom.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import rs.nms.newsroom.server.dto.ScheduleDTOs;
import rs.nms.newsroom.server.dto.ScheduleSearchCriteria;
import rs.nms.newsroom.server.dto.common.PagedResponse;
import rs.nms.newsroom.server.service.ScheduleService;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for scheduling management in newsroom and broadcast workflows.
 * <p>
 * Provides endpoints for creation, retrieval, filtering, updating, and deletion of schedule entries.
 * Supports advanced time-based and channel-based search, as well as pagination and sorting.
 * </p>
 */
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@Tag(
    name = "Schedule Management",
    description = "Comprehensive scheduling API for newsroom and broadcast workflows. Includes time range search, channel filtering, rundown association, and advanced pagination."
)
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(
        summary = "Create a new schedule entry",
        description = "Creates a new schedule entry for planned programs or newsroom segments."
    )
    @PostMapping
    public ResponseEntity<ScheduleDTOs.ScheduleResponse> create(
            @Valid @RequestBody ScheduleDTOs.ScheduleCreateRequest createRequest) {
        return ResponseEntity.ok(scheduleService.create(createRequest));
    }

    @Operation(
        summary = "Get schedule by ID",
        description = "Fetches a specific schedule entry by its unique identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTOs.ScheduleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }

    @Operation(
        summary = "Get all schedules",
        description = "Returns a list of all scheduled programs or segments."
    )
    @GetMapping
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getAll() {
        return ResponseEntity.ok(scheduleService.getAll());
    }

    @Operation(
        summary = "Get all schedules for a given rundown",
        description = "Fetches all schedule entries associated with a specific rundown."
    )
    @GetMapping("/rundown/{rundownId}")
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getByRundownId(
            @PathVariable Long rundownId) {
        return ResponseEntity.ok(scheduleService.getByRundownId(rundownId));
    }

    @Operation(
        summary = "Get all schedules for a channel",
        description = "Returns all schedule entries assigned to a particular channel."
    )
    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getByChannel(
            @PathVariable String channel) {
        return ResponseEntity.ok(scheduleService.getByChannel(channel));
    }

    @Operation(
        summary = "Get schedules by time range",
        description = "Returns all schedules falling within a given time interval (start/end in yyyy-MM-dd HH:mm:ss format)."
    )
    @GetMapping("/time-range")
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return ResponseEntity.ok(scheduleService.getByTimeRange(start, end));
    }
    
    @Operation(
        summary = "Get currently active schedules",
        description = "Returns all schedules that are currently in progress (active programs/segments)."
    )
    @GetMapping("/active")
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getCurrentActive() {
        return ResponseEntity.ok(scheduleService.getCurrentActive());
    }

    @Operation(
        summary = "Get upcoming schedules",
        description = "Returns all schedules planned to start in the future (upcoming programs/segments)."
    )
    @GetMapping("/upcoming")
    public ResponseEntity<List<ScheduleDTOs.ScheduleResponse>> getUpcoming() {
        return ResponseEntity.ok(scheduleService.getUpcoming());
    }
    
    @Operation(
        summary = "Search and filter schedules with pagination",
        description = "Advanced search for schedules using filters (channel, time, status, etc.), with pagination and sorting. Returns a paged response."
    )
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<ScheduleDTOs.ScheduleResponse>> searchSchedules(
            @RequestBody ScheduleSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        Sort.Order order = new Sort.Order(
                sort.length > 1 && sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        Page<ScheduleDTOs.ScheduleResponse> resultPage = scheduleService.searchSchedules(criteria, pageable);

        PagedResponse<ScheduleDTOs.ScheduleResponse> response = new PagedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPage(resultPage.getNumber());
        response.setSize(resultPage.getSize());
        response.setTotalElements(resultPage.getTotalElements());
        response.setTotalPages(resultPage.getTotalPages());
        response.setLast(resultPage.isLast());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update a schedule entry",
        description = "Modifies an existing schedule entry."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDTOs.ScheduleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleDTOs.ScheduleUpdateRequest updateRequest) {
        return ResponseEntity.ok(scheduleService.update(id, updateRequest));
    }

    @Operation(
        summary = "Delete a schedule entry",
        description = "Deletes a schedule entry by its unique identifier."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}