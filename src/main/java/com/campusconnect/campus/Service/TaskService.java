package com.campusconnect.campus.Service;

import com.campusconnect.campus.Dto.Request.TaskRequestDto;
import com.campusconnect.campus.Dto.Request.TaskUpdateRequestDto;
import com.campusconnect.campus.Dto.Response.TaskResponseDto;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Enum.TaskStatus;
import com.campusconnect.campus.Exception.BadRequestException;
import com.campusconnect.campus.Exception.EmployeeNotFoundException;
import com.campusconnect.campus.Exception.ResourceNotFoundException;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.Task;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.ProjectRepository;
import com.campusconnect.campus.Repositories.TaskRepository;
import com.campusconnect.campus.Repositories.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.crossstore.ChangeSetPersister;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.campusconnect.campus.Enum.Role.ROLE_EMPLOYEE;

@Service
public class TaskService {
     private final UserRepository userRepository;
     private final TaskRepository taskRepository;
     private final ProjectRepository projectRepository;
     public TaskService(TaskRepository taskRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository) {
         this.taskRepository = taskRepository;
         this.projectRepository = projectRepository;
         this.userRepository = userRepository;
     }

    public TaskResponseDto createTask(Long projectId, TaskRequestDto taskRequestDto, User currentUser) {

        Project project=projectRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(()->new ResourceNotFoundException("Project not found"));

        Long ownerId=project.getOwner().getId(); // Project owner id

        if(!Objects.equals(ownerId, currentUser.getId())){
            throw new UnauthorizedAccessException("You are not allowed to perform this action");
        }

        User employee=userRepository.findById(taskRequestDto.getAssignedToId())
                .orElseThrow(()->new EmployeeNotFoundException("Employee not found"));

        if(employee.getRole()!= Role.ROLE_EMPLOYEE){
            throw new EmployeeNotFoundException("please enter valid employee id ");
        }
        if(!employee.getEmailVerified()){
            throw new BadRequestException("Employee is not verified !");
        }

        Task task=new Task();
        task.setTitle(taskRequestDto.getTitle());
        task.setDescription(taskRequestDto.getDescription());
        task.setPriority(taskRequestDto.getPriority());
        task.setDueDate(taskRequestDto.getDueDate());
        task.setProject(project);
        task.setAssignedTo(employee);
        task.setStatus(TaskStatus.PENDING);
        Task dbSavedTask=taskRepository.save(task);

        return getTaskResponseDto(dbSavedTask, project, employee);

    }

    private static @NonNull TaskResponseDto getTaskResponseDto(Task dbSavedTask, Project project, User employee) {
        TaskResponseDto taskResponseDto=new TaskResponseDto();
        taskResponseDto.setTitle(dbSavedTask.getTitle());
        taskResponseDto.setDescription(dbSavedTask.getDescription());
        taskResponseDto.setPriority(dbSavedTask.getPriority());
        taskResponseDto.setDueDate(dbSavedTask.getDueDate());
        taskResponseDto.setId(dbSavedTask.getId()); // task id returns in this
        taskResponseDto.setStatus(dbSavedTask.getStatus());
        taskResponseDto.setProjectName(project.getProjectName()); // project name
        taskResponseDto.setAssignedToName(employee.getFullName()); // employee name
        taskResponseDto.setCreatedAt(dbSavedTask.getCreatedAt());
        return taskResponseDto;
    }

    // now update Task status

    public TaskResponseDto updateTaskStatus(Long taskId, User currentUser, TaskUpdateRequestDto taskUpdateRequestDto) {

         Task task=taskRepository.findById(taskId)
                .orElseThrow(()->new ResourceNotFoundException("Task not found"));
         // task object mill gya ensure kro current user wohi hoon jisne ko assigned hua hain task
        Long currentUserId=currentUser.getId(); //Employee
        if(currentUser.getRole()!= Role.ROLE_EMPLOYEE){
            throw new UnauthorizedAccessException("You are not allowed to perform this action");
        }
        if(!Objects.equals(task.getAssignedTo().getId(), currentUserId)){
            throw new UnauthorizedAccessException("You are not allowed to perform this action");
        }

        // agr current task ==>pending hoga
        TaskStatus taskStatus = getTaskStatus(taskUpdateRequestDto, task);
        task.setStatus(taskStatus);
        Task dbSavedTask=taskRepository.save(task);
        Project project=dbSavedTask.getProject();
        return getTaskResponseDto(dbSavedTask,project,currentUser);

    }
    // helper method for updating task-->basically Task status Lifecycle {PENDING--->PROGRESS--->DONE}
    private static TaskStatus getTaskStatus(TaskUpdateRequestDto taskUpdateRequestDto, Task task) {
        TaskStatus currentStatus= task.getStatus();
        TaskStatus taskStatus= taskUpdateRequestDto.getTaskStatus();
        if(currentStatus==TaskStatus.PENDING){
            if(taskStatus!=TaskStatus.IN_PROGRESS){
                throw new BadRequestException("Task status should be IN_PROGRESS");
            }
        }
        else if(currentStatus==TaskStatus.IN_PROGRESS){
            if(taskStatus!=TaskStatus.DONE){
                throw new BadRequestException("Task status should be DONE");
            }
        }
        else if(currentStatus==TaskStatus.DONE){
            throw new BadRequestException("Task already is done");
        }
        return taskStatus;
    }

    public Page<TaskResponseDto> getTaskByProjectId(Long projectId, Pageable pageable , User currentUser) {
         Project project= projectRepository.findById(projectId)
                 .orElseThrow(()->new ResourceNotFoundException("Project not found"));

         boolean projectOwner= Objects.equals(project.getOwner().getId(), currentUser.getId());

        boolean employee=taskRepository.existsByProjectIdAndAssignedTo(projectId,currentUser);

         if(!projectOwner && !employee){
             throw new UnauthorizedAccessException("You are not allowed to perform this action");
         }

         Page<Task> pageTask=taskRepository.findByProjectId(projectId,pageable);

         return pageTask.map(task->getTaskResponseDto(task,project,task.getAssignedTo()));
    }

    public List<TaskResponseDto> getMyAssignedTasks( User currentUser) {

        if(currentUser.getRole()!= ROLE_EMPLOYEE){
            throw new UnauthorizedAccessException("You are not allowed to perform this action");
        }
        List<Task> tasklist=taskRepository.findByAssignedTo(currentUser);
        return tasklist.stream()
                .map(task->getTaskResponseDto(task,task.getProject(),currentUser))
                .collect(Collectors.toList());
    }

    public String deleteTask(Long taskId, User currentUser) {
         Task task=taskRepository.findById(taskId)
                .orElseThrow(()->new ResourceNotFoundException("Task not found"));
         if(!Objects.equals(task.getProject().getOwner().getId(), currentUser.getId())){ // comparison of id
             throw new UnauthorizedAccessException("You are not allowed to perform this action");
         }
         String name =task.getTitle();
             taskRepository.delete(task);
        return "Task \""+name+" \"has been deleted";

    }
}
