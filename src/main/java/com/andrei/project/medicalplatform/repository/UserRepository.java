package com.andrei.project.medicalplatform.repository;

import com.andrei.project.medicalplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * You almost certainly already have a UserRepository (User rows are
 * referenced by id everywhere - managerId, userId on Doctor/Patient), so
 * this file is only a scaffold: if yours already exists, just add
 * findByRoleNameIgnoreCase to it instead of using this file, and delete
 * this one.
 *
 * findByRoleNameIgnoreCase traverses User's @ManyToMany "roles" collection
 * and matches on Role.name - given your Role entity (id, name,
 * @ManyToMany(mappedBy = "roles") Set<User> users), this assumes User has
 * the other side of that relationship as a field literally named "roles"
 * (a Set<Role> or List<Role>). If that field is named something else on
 * User, change ".roles" in the JPQL below to match (e.g. ".userRoles").
 *
 * This is deliberately case-insensitive and tolerant of a "ROLE_" prefix
 * (matches "DOCTOR", "doctor" or "ROLE_DOCTOR" alike), because the plain
 * exact-match findByRoles_Name(String) derived query was the reason every
 * "select a user" dropdown came back empty in practice - the query itself
 * worked, it just never found a row whose Role.name matched the literal
 * constant string exactly. If your role names really are stored some other
 * way entirely, tell me the actual values and I'll drop this in favor of an
 * exact match again.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRoles_Name(String roleName);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r " +
            "WHERE UPPER(r.name) = UPPER(:roleName) OR UPPER(r.name) = UPPER(CONCAT('ROLE_', :roleName))")
    List<User> findByRoleNameIgnoreCase(@Param("roleName") String roleName);
}
