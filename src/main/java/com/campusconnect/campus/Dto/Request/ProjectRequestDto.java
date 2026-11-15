package com.campusconnect.campus.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectRequestDto {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
}
