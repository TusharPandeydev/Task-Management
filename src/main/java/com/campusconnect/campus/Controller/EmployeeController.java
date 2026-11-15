package com.campusconnect.campus.Controller;

import com.campusconnect.campus.Dto.Response.EmployeeResponseDTO;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(@AuthenticationPrincipal User currentUser){
        List<EmployeeResponseDTO> employeeResponseDTOS = employeeService.getAllEmployees(currentUser);
        return ResponseEntity.ok(employeeResponseDTOS);
    }

}
