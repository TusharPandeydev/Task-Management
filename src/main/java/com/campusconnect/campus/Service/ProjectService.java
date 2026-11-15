package com.campusconnect.campus.Service;

import com.campusconnect.campus.Dto.Request.ProjectRequestDto;
import com.campusconnect.campus.Dto.Response.ProjectResponseDto;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Exception.BadRequestException;
import com.campusconnect.campus.Exception.ResourceNotFoundException;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    public ProjectService(ProjectRepository projectRepository) {

        this.projectRepository = projectRepository;
    }

    public ProjectResponseDto createProject(ProjectRequestDto  projectRequestDto , User owner){
        Project project = new Project();
        project.setProjectName(projectRequestDto.getName());
        project.setDescription(projectRequestDto.getDescription());
        project.setOwner(owner);
        project.setIsActive(true);
        Project dbSaved=projectRepository.save(project);

        ProjectResponseDto projectResponseDto=new ProjectResponseDto();

        projectResponseDto.setId(dbSaved.getId());
        projectResponseDto.setProjectName(dbSaved.getProjectName());
        projectResponseDto.setDescription(dbSaved.getDescription());
        projectResponseDto.setCreatedAt(dbSaved.getCreatedAt());

        projectResponseDto.setOwnerName(dbSaved.getOwner().getFullName());

        return projectResponseDto;

    }

   public List<ProjectResponseDto> getAllProjects(User currentUser){
        // owner jis jis project ka malik hain
       if(currentUser.getRole()!= Role.ROLE_OWNER){
           throw new UnauthorizedAccessException("not authorized");
       }

     List<Project> projects=projectRepository.findByOwnerAndIsActiveTrue(currentUser);

       return projects.stream()
               .map( project->new ProjectResponseDto(
                         project.getId(),
                         project.getProjectName(),
                        project.getDescription(),
                       project.getOwner().getFullName(),
                       project.getCreatedAt(),
                       project.getUpdatedAt()
                       )
               )
               .collect(Collectors.toList());
   }

   public ProjectResponseDto findByProjectId(Long id,User currentUser){
       Project project1=projectRepository.findById(id)
               .orElseThrow(()->new ResourceNotFoundException("Project not found"));
        if(project1.getIsActive()==false){
            throw new ResourceNotFoundException("Project not found");
        }
        if(currentUser.getRole()!= Role.ROLE_OWNER){
            throw new UnauthorizedAccessException("not authorized");
        }
        User user1=project1.getOwner();
        if(!Objects.equals(currentUser.getId(), user1.getId())){
            throw new UnauthorizedAccessException("You do not have access to this project");
        }
       return new ProjectResponseDto(
               project1.getId(),
               project1.getProjectName(),
               project1.getDescription(),
               project1.getOwner().getFullName(),
               project1.getCreatedAt(),
               project1.getUpdatedAt()
       );

   }

   public ProjectResponseDto updateProject(Long projectId,User currentUser , ProjectRequestDto projectRequestDto){
        Project getProject=projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if(getProject.getIsActive()==false){
            throw new ResourceNotFoundException("Project not found");
        }

        Long projectOwner=getProject.getOwner().getId(); // By this we gets project owner ID
       if(currentUser.getRole()!= Role.ROLE_OWNER){
           throw new UnauthorizedAccessException("not authorized");
       }

        if(!Objects.equals(currentUser.getId(), projectOwner)){
            throw new UnauthorizedAccessException("You do not have access to this project");
        }
        if(projectRequestDto.getName()==null && projectRequestDto.getDescription()==null){
            throw new BadRequestException("Please update first then click Update button");
        }

        ProjectResponseDto projectResponseDto=new ProjectResponseDto();
        projectResponseDto.setId(projectId);
        projectResponseDto.setOwnerName(currentUser.getFullName());

        if(projectRequestDto.getName()!=null){
            getProject.setProjectName(projectRequestDto.getName()); // Original Project
            projectResponseDto.setProjectName(projectRequestDto.getName()); // for response
        }
        if(projectRequestDto.getDescription()!=null){
            getProject.setDescription(projectRequestDto.getDescription());
            projectResponseDto.setDescription(projectRequestDto.getDescription());
        }

        Project dbSaved=projectRepository.save(getProject);
        projectResponseDto.setUpdatedAt(dbSaved.getUpdatedAt());
        projectResponseDto.setCreatedAt(dbSaved.getCreatedAt());

      return projectResponseDto;
   }

   public String deleteProject(Long projectId, User currentUser){
     Project  project=projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
     // first check weather the project which we are working on is active or not
       if(project.getIsActive()==false){
           throw new ResourceNotFoundException("Project not found");
       }
     if(currentUser.getRole()!= Role.ROLE_OWNER){
         throw new UnauthorizedAccessException("not authorized");
     }

     Long ownerId=project.getOwner().getId();
     if(!Objects.equals(currentUser.getId(), ownerId)){
         throw new UnauthorizedAccessException("You do not have access to this project");
     }
     String projectName=project.getProjectName();
     project.setIsActive(false);
     projectRepository.save(project);
       return projectName+" has been successfully deleted";

   }
}
