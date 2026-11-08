package com.university.helpdesk.repository;
 
import com.university.helpdesk.entity.model.Role;
import com.university.helpdesk.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 
    // For login, optional because it may blow up if the username searched does not exist
    Optional<User> findByUsername(String username);
    
    //check if username exists
    boolean existsByUsername(String username);
 
    // Find users by role
    List<User> findByRole(Role role);
}
