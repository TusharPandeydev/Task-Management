package com.campusconnect.campus.Dto.Request;

import com.campusconnect.campus.Enum.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class TaskRequestDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message="Please enter description")
    private String description;

    private TaskPriority priority;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

   
    @NotNull(message="Mention employeeID !")
    private Long assignedToId;
}
