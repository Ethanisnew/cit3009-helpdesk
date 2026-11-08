package com.university.helpdesk.entity.model;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "id")   // PK and FK to users.id
public class Administrator extends User {

	//cardinality
    // Administrator assigns many Tickets
    @OneToMany(mappedBy = "admin")
    private List<Ticket> assignedTickets = new ArrayList<>();
    //	Construcutor
    public Administrator() {
        setRole(Role.ADMIN);
    }

    public Administrator(String username, String password, String name) {
        super(username, password, name, Role.ADMIN);
    }
    
    public List<Ticket> getAssignedTickets() { return assignedTickets; }
}