package com.campusconnect.campus.Service;

import com.campusconnect.campus.Dto.Response.EmployeeResponseDTO;
import com.campusconnect.campus.Enum.Role;
import com.campusconnect.campus.Exception.UnauthorizedAccessException;
import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final UserRepository userRepository;
    public EmployeeService( UserRepository userRepository) {
         this.userRepository = userRepository;
    }
    private static EmployeeResponseDTO getEmployee(User currentUser){
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        employeeResponseDTO.setId(currentUser.getId());
        employeeResponseDTO.setEmail(currentUser.getEmail());
        employeeResponseDTO.setFullName(currentUser.getFullName());
        return employeeResponseDTO;
    }
    public List<EmployeeResponseDTO> getAllEmployees(User currentUser){
        if(currentUser.getRole()!= Role.ROLE_OWNER){
            throw new UnauthorizedAccessException("Unauthorized Access");
        }
        List<User> verifiedUsers=userRepository.findByRoleAndEmailVerifiedTrue(Role.ROLE_EMPLOYEE);
        return verifiedUsers.stream()
                .map(verifiedUser->getEmployee(verifiedUser))
                .collect(Collectors.toList());
    }
}
