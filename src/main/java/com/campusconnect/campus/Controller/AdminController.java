package com.campusconnect.campus.Controller;

import com.campusconnect.campus.Dto.Response.EmployeeResponseDTO;
import com.campusconnect.campus.Model.User;
import com.campusconnect.campus.Service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    private ResponseEntity<Map<String,Object>> buildResponse(String message){
        Map<String,Object> map=new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        map.put("status", HttpStatus.OK.value()); // 401 etc WHY NOR JUST STATUS
        map.put("error", HttpStatus.OK.getReasonPhrase()); // reason in string format "Not found"
        map.put("message",message);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{employeeId}")
    public ResponseEntity<Map<String,Object>> promoteToOwner(@PathVariable("employeeId") Long userId,
     @AuthenticationPrincipal User currentUser){
        String message = adminService.promoteToOwner(userId,currentUser);
        return buildResponse(message);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> findAllEmployees(@AuthenticationPrincipal User currentUser){
         List<EmployeeResponseDTO> list=adminService.findAllEmployees(currentUser);
         return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
