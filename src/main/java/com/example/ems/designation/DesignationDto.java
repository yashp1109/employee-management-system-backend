package com.example.ems.designation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignationDto(
        Long id,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        Long departmentId,
        String departmentName
) {
}
