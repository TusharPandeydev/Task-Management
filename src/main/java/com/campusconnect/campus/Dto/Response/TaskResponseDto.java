package com.campusconnect.campus.Dto.Response;

import com.campusconnect.campus.Enum.TaskPriority;
import com.campusconnect.campus.Enum.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private String projectName;
    private String assignedToName;
    private LocalDateTime createdAt;
}
