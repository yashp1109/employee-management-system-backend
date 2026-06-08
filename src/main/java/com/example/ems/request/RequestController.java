package com.example.ems.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.example.ems.request.RequestDtos.*;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService service;

    @PostMapping("/api/requests/profile-update")
    RequestDto createProfileUpdate(@Valid @RequestBody ProfileUpdateRequestDto request) {
        return service.createProfileUpdate(request);
    }

    @GetMapping("/api/requests")
    Page<RequestDto> myRequests(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) RequestType type,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return service.search(employeeId, type, status, pageable);
    }

    @GetMapping("/api/requestsAdmin/HR")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    Page<RequestDto> adminRequests(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) RequestType type,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return service.search(employeeId, type, status, pageable);
    }

    @PutMapping("/api/requests/{id}/action")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    RequestDto actionRequest(@PathVariable Long id, @Valid @RequestBody RequestActionDto action) {
        return service.action(id, action);
    }
}
