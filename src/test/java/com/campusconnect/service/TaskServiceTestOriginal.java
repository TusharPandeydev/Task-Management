package com.campusconnect.service;

import com.campusconnect.campus.Dto.Request.TaskRequestDto;
import com.campusconnect.campus.Dto.Request.TaskUpdateRequestDto;
import com.campusconnect.campus.Dto.Response.TaskResponseDto;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Enum.TaskPriority;
import com.campusconnect.campus.Enum.TaskStatus;
import com.campusconnect.campus.Exception.BadRequestException;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.Task;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.ProjectRepository;
import com.campusconnect.campus.Repositories.TaskRepository;
import com.campusconnect.campus.Repositories.UserRepository;
import com.campusconnect.campus.Service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTestOriginal {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService( // we need all three mockito repositories for this
                taskRepository,
                projectRepository,
                userRepository
        );
    }
    @Test
    void createTask_whenOwnerAndVerifiedEmployee_thenTaskCreated(){
        // project owner
        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Project Owner");
        owner.setRole(Role.ROLE_OWNER);

        // employee
        User employee = new User();
        employee.setId(2L);
        employee.setFullName("Test Employee");
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setEmailVerified(true);

        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);
        when(projectRepository.findByIdAndIsActiveTrue(10L))
                .thenReturn(Optional.of(project));

        // 5. Create task request
        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Test Task");
        request.setDescription("Testing task creation");
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDateTime.now().plusDays(2));
        request.setAssignedToId(2L);

        // 6. Tell mock UserRepository what to return
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(employee));

        // 7. Create the task that mock repository will return after save()
        Task savedTask = new Task();
        savedTask.setId(100L);
        savedTask.setTitle("Test Task");
        savedTask.setDescription("Testing task creation");
        savedTask.setPriority(TaskPriority.HIGH);
        savedTask.setDueDate(request.getDueDate());
        savedTask.setProject(project);
        savedTask.setAssignedTo(employee);
        savedTask.setStatus(TaskStatus.PENDING);

        // 8. Tell mock TaskRepository what to return when save() is called
        when(taskRepository.save(any()))
                .thenReturn(savedTask);

        // 9. Call the ACTUAL TaskService method
        TaskResponseDto response =
                taskService.createTask(10L, request, owner);

        // 10. Verify the result
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Employee", response.getAssignedToName());
        assertEquals("TaskFlow Project", response.getProjectName());
        assertEquals(TaskStatus.PENDING, response.getStatus());
    }
    @Test
    void createTask_whenUserIsNotProjectOwner_thenThrowUnauthorizedAccessException() {

        // 1. Actual project owner
        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Actual Owner");
        owner.setRole(Role.ROLE_OWNER);

        // 2. User who is trying to create the task
        User currentUser = new User();
        currentUser.setId(99L);
        currentUser.setFullName("Other User");
        currentUser.setRole(Role.ROLE_OWNER);

        // 3. Project belongs to owner ID = 1
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);

        // 4. Mock project repository
        when(projectRepository.findByIdAndIsActiveTrue(10L))
                .thenReturn(Optional.of(project));

        // 5. Request data
        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Unauthorized Task");
        request.setDescription("This task should not be created");
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDateTime.now().plusDays(2));
        request.setAssignedToId(2L);

        // 6. Actual TaskService method is called
        assertThrows(
                UnauthorizedAccessException.class,
                () -> taskService.createTask(10L, request, currentUser)
        );

        // 7. Since authorization failed, these repositories
        // should never be called
        verify(userRepository, never()).findById(2L);
        verify(taskRepository, never()).save(any());
    }
    @Test
    void createTask_whenEmployeeIsNotVerified_thenThrowBadRequestException() {

        // Project owner
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.ROLE_OWNER);

        // Unverified employee
        User employee = new User();
        employee.setId(2L);
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setEmailVerified(false);

        // Project
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);
        project.setIsActive(true);

        // Mock project lookup
        when(projectRepository.findByIdAndIsActiveTrue(10L))
                .thenReturn(Optional.of(project));

        // Task request
        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Test Task");
        request.setDescription("Testing unverified employee");
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDateTime.now().plusDays(2));
        request.setAssignedToId(2L);

        // Mock employee lookup
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(employee));

        // Actual service call should throw exception
        assertThrows(
                BadRequestException.class,
                () -> taskService.createTask(10L, request, owner)
        );

        // Task must NOT be saved
        verify(taskRepository, never()).save(any());
    }
    @Test
    void updateTaskStatus_whenPendingAndRequestedInProgress_thenSuccess() {

        // Employee
        User employee = new User();
        employee.setId(2L);
        employee.setFullName("Test Employee");
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setEmailVerified(true);

        // Project
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");

        // Task currently PENDING
        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setDescription("Testing status update");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);
        task.setAssignedTo(employee);

        // Request: PENDING -> IN_PROGRESS
        TaskUpdateRequestDto request = new TaskUpdateRequestDto();
        request.setTaskStatus(TaskStatus.IN_PROGRESS);

        // Mock repository
        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any()))
                .thenReturn(task);

        // Actual TaskService method
        TaskResponseDto response =
                taskService.updateTaskStatus(100L, employee, request);

        // Verify result
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());

        // Verify save happened
        verify(taskRepository).save(task);
    }
    @Test
    void updateTaskStatus_whenInProgressAndRequestedDone_thenSuccess() {

        // Employee
        User employee = new User();
        employee.setId(2L);
        employee.setFullName("Test Employee");
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setEmailVerified(true);

        // Project
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");

        // Task currently IN_PROGRESS
        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setDescription("Testing status update");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);
        task.setAssignedTo(employee);

        // Request: IN_PROGRESS -> DONE
        TaskUpdateRequestDto request = new TaskUpdateRequestDto();
        request.setTaskStatus(TaskStatus.DONE);

        // Mock repository
        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any()))
                .thenReturn(task);

        // Actual TaskService method
        TaskResponseDto response =
                taskService.updateTaskStatus(100L, employee, request);

        // Verify result
        assertEquals(TaskStatus.DONE, response.getStatus());

        // Verify save happened
        verify(taskRepository).save(task);
    }
    @Test
    void updateTaskStatus_whenPendingAndRequestedDone_thenThrowBadRequestException() {

        // Employee
        User employee = new User();
        employee.setId(2L);
        employee.setRole(Role.ROLE_EMPLOYEE);

        // Project
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");

        // Task currently PENDING
        Task task = new Task();
        task.setId(100L);
        task.setStatus(TaskStatus.PENDING);
        task.setProject(project);
        task.setAssignedTo(employee);

        // Request: PENDING -> DONE
        TaskUpdateRequestDto request = new TaskUpdateRequestDto();
        request.setTaskStatus(TaskStatus.DONE);

        // Mock repository
        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        // Actual service should reject this transition
        assertThrows(
                BadRequestException.class,
                () -> taskService.updateTaskStatus(100L, employee, request)
        );

        // Task must not be saved
        verify(taskRepository, never()).save(any());
    }
    @Test
    void updateTaskStatus_whenWrongEmployee_thenThrowUnauthorizedAccessException() {

        // Employee to whom task is actually assigned
        User assignedEmployee = new User();
        assignedEmployee.setId(2L);
        assignedEmployee.setFullName("Assigned Employee");
        assignedEmployee.setRole(Role.ROLE_EMPLOYEE);

        // Current user trying to update the task
        User currentUser = new User();
        currentUser.setId(99L);
        currentUser.setFullName("Other Employee");
        currentUser.setRole(Role.ROLE_EMPLOYEE);

        // Project
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");

        // Task belongs to assignedEmployee
        Task task = new Task();
        task.setId(100L);
        task.setStatus(TaskStatus.PENDING);
        task.setProject(project);
        task.setAssignedTo(assignedEmployee);

        // Request
        TaskUpdateRequestDto request = new TaskUpdateRequestDto();
        request.setTaskStatus(TaskStatus.IN_PROGRESS);

        // Mock repository
        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        // Actual service should reject the current user
        assertThrows(
                UnauthorizedAccessException.class,
                () -> taskService.updateTaskStatus(
                        100L,
                        currentUser,
                        request
                )
        );

        // Task must not be saved
        verify(taskRepository, never()).save(any());
    }
    @Test
    void getTaskByProjectId_whenUserHasNoAccess_thenThrowUnauthorizedAccessException() {

        // Project owner
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.ROLE_OWNER);

        // User trying to access the project
        User currentUser = new User();
        currentUser.setId(99L);
        currentUser.setRole(Role.ROLE_EMPLOYEE);

        // Project belongs to owner
        Project project = new Project();
        project.setId(10L);
        project.setProjectName("TaskFlow Project");
        project.setOwner(owner);

        // Mock project lookup
        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(project));

        // Current user is NOT assigned to any task of this project
        when(taskRepository.existsByProjectIdAndAssignedTo(
                10L, currentUser))
                .thenReturn(false);

        // Actual service call
        assertThrows(
                UnauthorizedAccessException.class,
                () -> taskService.getTaskByProjectId(
                        10L,
                        Pageable.unpaged(),
                        currentUser
                )
        );

        // Since access was denied, tasks should never be fetched

    }

}
