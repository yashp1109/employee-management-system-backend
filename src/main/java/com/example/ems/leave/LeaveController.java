package com.example.ems.leave;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.ems.leave.LeaveDtos.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService service;

    @PostMapping
    LeaveRequestDto apply(@RequestBody ApplyLeaveRequest request) {
        return service.apply(request);
    }

    @GetMapping
    Page<LeaveRequestDto> list(
            @RequestParam(required = false) Long employeeId,
            Pageable pageable
    ) {
        return service.list(employeeId, pageable);
    }

    @GetMapping("/history")
    Page<LeaveRequestDto> history(Pageable pageable) {
        return service.myHistory(pageable);
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    Page<LeaveRequestDto> pendingApprovals(Pageable pageable) {
        return service.pendingApprovals(pageable);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    LeaveRequestDto approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    LeaveRequestDto reject(@PathVariable Long id) {
        return service.reject(id);
    }

    @PatchMapping("/{id}/cancel")
    LeaveRequestDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/balances")
    List<LeaveBalanceDto> balances() {
        return service.balances();
    }
}
