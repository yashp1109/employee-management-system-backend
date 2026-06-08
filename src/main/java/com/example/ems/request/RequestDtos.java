package com.example.ems.request;

import com.example.ems.common.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public class RequestDtos {
    public record ProfileUpdateRequestDto(
            @NotNull Long employeeId,
            @NotBlank String comment,
            Map<String, Object> payload
    ) {
    }

    public record RequestActionDto(
            @NotBlank String action,
            String reviewerComment
    ) {
    }

    public record RequestDto(
            Long id,
            RequestType type,
            Long employeeId,
            String employeeName,
            Long requestedById,
            String requestedByName,
            String comment,
            RequestStatus status,
            Long reviewerId,
            String reviewerName,
            String reviewerComment,
            Instant createdAt,
            Instant updatedAt,
            Map<String, Object> payload
    ) {
    }
}
