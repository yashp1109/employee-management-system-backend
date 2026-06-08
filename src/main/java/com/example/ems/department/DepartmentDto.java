package com.example.ems.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentDto(
        Long id,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description
) {
}
