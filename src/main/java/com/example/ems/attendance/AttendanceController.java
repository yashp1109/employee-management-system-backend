package com.example.ems.attendance;

import com.example.ems.common.ApiResonse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService service;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn() {
        return ResponseEntity.ok(service.checkIn());
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut() {
        return ResponseEntity.ok(service.checkOut());
    }

    @GetMapping
    Page<AttendanceDto> history(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            Pageable pageable
    ) {
        LocalDate today = LocalDate.now();
        return service.history(employeeId, start == null ? today.withDayOfMonth(1) : start, end == null ? today : end, pageable);
    }
}
