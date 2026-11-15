package com.campusconnect.campus.Controller;

import com.campusconnect.campus.Dto.Request.TaskRequestDto;
import com.campusconnect.campus.Dto.Request.TaskUpdateRequestDto;
import com.campusconnect.campus.Dto.Response.TaskResponseDto;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Service.TaskService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{projectId}/create-task")
    public ResponseEntity<TaskResponseDto> createTask(@PathVariable("projectId") Long projectId,
                                               @Valid @RequestBody TaskRequestDto taskRequestDto,
                                                      @AuthenticationPrincipal User user){
        TaskResponseDto taskResponseDto=taskService.createTask(projectId,taskRequestDto,user);
        return ResponseEntity.ok(taskResponseDto);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(@PathVariable("taskId") Long taskId,
                                                      @Valid @RequestBody TaskUpdateRequestDto taskUpdateRequestDto,
                                                      @AuthenticationPrincipal User currentUser){
             TaskResponseDto taskResponseDto=taskService.updateTaskStatus(taskId,currentUser,taskUpdateRequestDto);
             return ResponseEntity.ok(taskResponseDto);
    }
   @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponseDto>> getTaskByProjectId(@PathVariable("projectId") Long projectId,
                                                                    Pageable pageable,
                                                                    @AuthenticationPrincipal User currentUser){
        Page<TaskResponseDto> taskResponseDtoPage=taskService.getTaskByProjectId(projectId,pageable,currentUser);
        return ResponseEntity.ok(taskResponseDtoPage);
    }

    @GetMapping("/my-task")
    public ResponseEntity<List<TaskResponseDto>> getAllMyAssignedTasks(@AuthenticationPrincipal User currentUser){
        List<TaskResponseDto> taskResponseDtoList=taskService.getMyAssignedTasks(currentUser);
        return ResponseEntity.ok(taskResponseDtoList);
    }
    private ResponseEntity<Map<String,Object>> buildResponse(String message){
        Map<String,Object> map=new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        map.put("status", HttpStatus.OK.value()); // 401 etc WHY NOR JUST STATUS
        map.put("error", HttpStatus.OK.getReasonPhrase()); // reason in string format "Not found"
        map.put("message",message);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    @PreAuthorize("hasRole('OWNER')")
     @DeleteMapping("/{taskId}")
    public ResponseEntity<Map<String,Object>> deleteTask(@PathVariable("taskId") Long taskId,
                                             @AuthenticationPrincipal User currentUser){
        String message=taskService.deleteTask(taskId,currentUser);
       return buildResponse(message);
     }
}
