package com.example.ems.designation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {
    private final DesignationService service;

    @GetMapping
    List<DesignationDto> list() {
        return service.list();
    }

    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    DesignationDto create( @RequestBody DesignationDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    DesignationDto update(@PathVariable Long id, @RequestBody DesignationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
