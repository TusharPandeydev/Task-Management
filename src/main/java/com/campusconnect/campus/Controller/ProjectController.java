package com.campusconnect.campus.Controller;

import com.campusconnect.campus.Dto.Request.ProjectRequestDto;
import com.campusconnect.campus.Dto.Response.ProjectResponseDto;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/create-project")
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto projectRequestDto,
                                                            @AuthenticationPrincipal User currentUser){
            ProjectResponseDto projectResponseDto = projectService.createProject(projectRequestDto,currentUser);
        return new ResponseEntity<>(projectResponseDto, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects(@AuthenticationPrincipal User currentUser){
        List<ProjectResponseDto> projectResponseDto = projectService.getAllProjects(currentUser);
        return new ResponseEntity<>(projectResponseDto, HttpStatus.OK);
    }

   @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{projectID}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable("projectID") Long projectID,
                                                         @AuthenticationPrincipal User currentUser){
        ProjectResponseDto projectResponseDto=projectService.findByProjectId(projectID,currentUser);
        return new ResponseEntity<>(projectResponseDto, HttpStatus.OK);
   }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable("projectId") Long projectId,
                                                            @Valid @RequestBody ProjectRequestDto projectRequestDto,
                                                           @AuthenticationPrincipal User currentUser ){
        ProjectResponseDto projectResponseDto=projectService.updateProject(projectId,currentUser,projectRequestDto);
        return new ResponseEntity<>(projectResponseDto, HttpStatus.OK);
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
   @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String,Object>> softDeleteProject(@PathVariable("projectId") Long projectId,
                                                    @AuthenticationPrincipal User currentUser){
        String message=projectService.deleteProject(projectId,currentUser);
        return buildResponse(message);
   }
}
