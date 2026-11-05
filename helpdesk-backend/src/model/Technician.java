package com.helpdesk.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "technicians")
@PrimaryKeyJoinColumn(name = "id")   // PK and FK to users.id
public class Technician extends User {
	//Attributes
    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 100)
    private String specialization;

    //Cardinality
    //Technician is assigned many Tickets
    @OneToMany(mappedBy = "technician")
    private List<Ticket> assignedTickets = new ArrayList<>();

    public Technician() {
        setRole(Role.TECHNICIAN);
    }

    public Technician(String username, String password, String name, String specialization) {
        super(username, password, name, Role.TECHNICIAN);
        this.specialization = specialization;
    }
    //Setter and getters
    public boolean isActive(){ 
    	return active; 
    }
    public void setActive(boolean active){ 
    	this.active = active; 
    }
    public String getSpecialization(){ 
    	return specialization;
    }
    public void setSpecialization(String specialization){ 
    	this.specialization = specialization;
    }
   
    public List<Ticket> getAssignedTickets(){ 
    	return assignedTickets; 
    }
}