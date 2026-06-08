package com.example.ems.employee;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService service;

    @GetMapping
    Page<EmployeeDto> search(@RequestParam(required = false) String q, Pageable pageable) {
        return service.search(q, pageable);
    }

    @GetMapping("/me")
    EmployeeDto me() {
        return service.getMe();
    }

    @GetMapping("/{id}")
    EmployeeDto get(@PathVariable Long id) {
        return service.getDto(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    EmployeeDto create(@Valid @RequestBody EmployeeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    EmployeeDto update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
