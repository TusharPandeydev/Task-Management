package com.campusconnect.campus.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String projectName;
    private String description;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
