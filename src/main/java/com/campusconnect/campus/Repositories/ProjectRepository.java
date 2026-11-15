package com.campusconnect.campus.Repositories;

import com.campusconnect.campus.Model.Project;
import com.campusconnect.campus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
    List<Project> findByOwnerAndIsActiveTrue(User owner);
    Optional<Project> findByIdAndIsActiveTrue(Long id);

    // get the list of projects which is owned by user object which we are passing
}
