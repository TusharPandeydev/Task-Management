package com.campusconnect.campus.Service;

import com.campusconnect.campus.Dto.Response.EmployeeResponseDTO;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Exception.AlreadyOwnerException;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Exception.UserNotFoundException;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String promoteToOwner(Long targetUserID,User currentUser){
        if(currentUser.getRole()!=Role.ROLE_ADMIN){
            throw new UnauthorizedAccessException("Not allowed to perform this action");
        }
        User user1 =userRepository.findById(targetUserID)
                .orElseThrow(()->new UserNotFoundException("User not found"));
        if(user1.getRole()== Role.ROLE_OWNER){
            throw new AlreadyOwnerException("User is already promoted!");
        }
        user1.setRole(Role.ROLE_OWNER);
        userRepository.save(user1);
        return user1.getUsername()+" is promoted!";
    }

    private static EmployeeResponseDTO getResponseDto(User currentUser){
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        employeeResponseDTO.setId(currentUser.getId());
        employeeResponseDTO.setFullName(currentUser.getFullName());
        employeeResponseDTO.setEmail(currentUser.getEmail());
        return employeeResponseDTO;
    }

    public List<EmployeeResponseDTO> findAllEmployees(User currentUser){
        if(currentUser.getRole()!=Role.ROLE_ADMIN){
            throw new UnauthorizedAccessException("Not allowed to perform this action");
        }
        List<User> list = userRepository.findByRoleAndEmailVerifiedTrue(Role.ROLE_EMPLOYEE);
        return list.stream()
                .map(employee->getResponseDto(employee))
                .collect(Collectors.toList());
    }
}
