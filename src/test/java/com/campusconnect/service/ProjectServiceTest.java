package com.campusconnect.service;


import com.campusconnect.campus.Dto.Request.ProjectRequestDto;
import com.campusconnect.campus.Dto.Response.ProjectResponseDto;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.ProjectRepository;
import com.campusconnect.campus.Service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

;import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository);
    }


    // =========================================================
    // 1. CREATE PROJECT - SUCCESS
    // =========================================================

    @Test
    void createProject_whenValidOwner_thenProjectCreated() {

        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Project Owner");

        owner.setRole(Role.ROLE_OWNER);

        ProjectRequestDto request = new ProjectRequestDto();
        request.setName("TaskFlow Project");
        request.setDescription("Project for task management");

        Project savedProject = new Project();
        savedProject.setId(10L);
        savedProject.setProjectName("TaskFlow Project");
        savedProject.setDescription("Project for task management");
        savedProject.setOwner(owner);
        savedProject.setIsActive(true);

        when(projectRepository.save(any(Project.class)))
                .thenReturn(savedProject);

        ProjectResponseDto response =
                projectService.createProject(request, owner);

        assertEquals(10L, response.getId());
        assertEquals("TaskFlow Project", response.getProjectName());
        assertEquals(
                "Project for task management",
                response.getDescription()
        );
        assertEquals("Project Owner", response.getOwnerName());

        verify(projectRepository).save(any(Project.class));
    }



    // =========================================================
    // 2. GET ALL PROJECTS - NON OWNER REJECTED
    // =========================================================

    @Test
    void getAllProjects_whenUserIsNotOwner_thenThrowUnauthorizedAccessException() {

        User employee = new User();
        employee.setId(2L);
        employee.setRole(Role.ROLE_EMPLOYEE);

        assertThrows(
                UnauthorizedAccessException.class,
                () -> projectService.getAllProjects(employee)
        );

        verify(projectRepository, never())
                .findByOwnerAndIsActiveTrue(employee);
    }




    @Test
    void findByProjectId_whenUserIsNotProjectOwner_thenThrowUnauthorizedAccessException() {

        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Actual Owner");
        owner.setRole(Role.ROLE_OWNER);

        User otherOwner = new User();
        otherOwner.setId(99L);
        otherOwner.setFullName("Other Owner");
        otherOwner.setRole(Role.ROLE_OWNER);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setDescription("Task management project");
        project.setOwner(owner);
        project.setIsActive(true);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> projectService.findByProjectId(
                        10L,
                        otherOwner
                )
        );
    }


    // =========================================================
    // 4. UPDATE PROJECT - OWNER SUCCESSFULLY UPDATES
    // =========================================================

    @Test
    void updateProject_whenOwnerUpdatesProject_thenSuccess() {

        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Project Owner");
        owner.setRole(Role.ROLE_OWNER);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("Old Project Name");
        project.setDescription("Old Description");
        project.setOwner(owner);
        project.setIsActive(true);

        ProjectRequestDto request = new ProjectRequestDto();
        request.setName("Updated Project");
        request.setDescription("Updated Description");

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        ProjectResponseDto response =
                projectService.updateProject(
                        10L,
                        owner,
                        request
                );

        assertEquals(10L, response.getId());
        assertEquals(
                "Updated Project",
                response.getProjectName()
        );
        assertEquals(
                "Updated Description",
                response.getDescription()
        );
        assertEquals(
                "Project Owner",
                response.getOwnerName()
        );

        verify(projectRepository).save(project);
    }




    @Test
    void updateProject_whenUserIsNotProjectOwner_thenThrowUnauthorizedAccessException() {

        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.ROLE_OWNER);

        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setRole(Role.ROLE_OWNER);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);

        ProjectRequestDto request = new ProjectRequestDto();
        request.setName("Hacked Project");
        request.setDescription("Unauthorized update");

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> projectService.updateProject(
                        10L,
                        otherUser,
                        request
                )
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }


    // =========================================================
    // 6. DELETE PROJECT - SOFT DELETE SUCCESS
    // =========================================================

    @Test
    void deleteProject_whenOwnerDeletesProject_thenSoftDeleteProject() {

        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Project Owner");
        owner.setRole(Role.ROLE_OWNER);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        String response =
                projectService.deleteProject(
                        10L,
                        owner
                );

        assertEquals(
                "TaskFlow Project has been successfully deleted",
                response
        );

        // Main proof of soft delete
        assertEquals(false, project.getIsActive());

        verify(projectRepository).save(project);
    }




    @Test
    void deleteProject_whenUserIsNotProjectOwner_thenThrowUnauthorizedAccessException() {

        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.ROLE_OWNER);

        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setRole(Role.ROLE_OWNER);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);

        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> projectService.deleteProject(
                        10L,
                        otherUser
                )
        );

        verify(projectRepository, never())
                .save(any(Project.class));
    }
}
