package com.campusconnect.campus.Repositories;

import com.campusconnect.campus.Model.Task;
import com.campusconnect.campus.Model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    List<Task> findByAssignedTo(User assignedTo);
    Boolean existsByProjectIdAndAssignedTo(Long projectId, User user);

}
