package com.campusconnect.campus.Dto.Request;

import com.campusconnect.campus.Enum.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskUpdateRequestDto {
    @NotNull(message = "Task status is required")
    private TaskStatus taskStatus;
}
