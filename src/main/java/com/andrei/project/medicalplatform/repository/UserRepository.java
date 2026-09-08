package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRoles_Name(String roleName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r " +
            "WHERE UPPER(r.name) = UPPER(:roleName) OR UPPER(r.name) = UPPER(CONCAT('ROLE_', :roleName))")
    List<User> findByRoleNameIgnoreCase(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.roles IS EMPTY")
    List<User> findUsersWithNoRoles();
}
