package com.helpdesk.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)

public class User {
	
	//Creating attribute
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
 
    @Column(nullable = false, unique = true, length = 50)
    private String username;
 
    @Column(nullable = false)
    private String password;
 
    @Column(nullable = false, length = 100)
    private String name;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.STUDENT_STAFF;
    
    //Creating cardinlity
    //User submits many Tickets
    @OneToMany(mappedBy = "user")
    private List<Ticket> tickets = new ArrayList<>();
 
    //User writes many Comments
    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();
 
    public User() {
    	
    }
 
    public User(String username, String password, String name, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }
    //Getters and Setters
    public Long getId(){
    	return id; 
    }
    public String getUsername(){ 
    	return username;
    }
    public void setUsername(String username){
    	this.username = username;
    }
    public String getPassword(){
    	return password;
    }
    public void setPassword(String password){
    	this.password = password;
    }
    public String getName(){ 
    	return name; 
    }
    public void setName(String name){ 
    
    	this.name = name; 
    }
    public Role getRole(){ 
    	return role;
    }
    public void setRole(Role role){ 
    	this.role = role; 
    }
   
    public List<Ticket> getTickets(){ 
    	return tickets; 
    }
    public List<Comment> getComments(){ 
    	return comments; 
    }
}
