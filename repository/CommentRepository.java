package com.helpdesk.repository;
 
import com.helpdesk.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
 
    //All comments on a ticket in order of oldest to newest
	//THe get comments method in tickets can also be used but this one orders them
    List<Comment> findByTicket_TicketIdOrderByDateCreatedAsc(Long ticketId);
    
    //All comments made by a user
    List<Comment> findByUser_Id(Long userId);
}