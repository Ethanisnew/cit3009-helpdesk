package com.university.helpdesk.repository;
 
import com.university.helpdesk.entity.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {
 
    // Technicians who can currently be assigned tickets
    List<Technician> findByActiveTrue();
    
    // All technicians of a certain specialization
    List<Technician> findBySpecialization(String specialization);
}