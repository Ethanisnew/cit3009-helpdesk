package com.university.helpdesk.repository;
 
import com.university.helpdesk.entity.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
	//Wow, such empty
}
