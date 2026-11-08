package com.university.helpdesk.repository;
 
import com.university.helpdesk.entity.model.Priority;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
 
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
 
 
    // Shows ticvkets submitted by staff/students
    List<Ticket> findByUser_Id(Long userId);
 
    // Shows tickets assigned to a tech
    List<Ticket> findByTechnician_Id(Long technicianId);
 
    // shows tickets assigned by an admin
    List<Ticket> findByAdmin_Id(Long adminId);
    
    // Find by:
    //status
    List<Ticket> findByStatus(Status status);
    //priority
    List<Ticket> findByPriority(Priority priority);
    //date range 
    List<Ticket> findByCreationDateBetween(LocalDateTime from, LocalDateTime to);
 
    //Unassigned tickets
    List<Ticket> findByTechnicianIsNull();
 
    //Basically so they can search using some parameters without having to use all of them
    //kinda like a filter

    // runs the query when calling the method
    @Query("""
            SELECT t FROM Ticket t
            LEFT JOIN t.user u
            LEFT JOIN t.technician tech
            WHERE (:ticketId IS NULL OR t.ticketId = :ticketId)
              AND (:userId   IS NULL OR u.id       = :userId)
              AND (:techId   IS NULL OR tech.id    = :techId)
              AND (:status   IS NULL OR t.status   = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:fromDate IS NULL OR t.creationDate >= :fromDate)
              AND (:toDate   IS NULL OR t.creationDate <= :toDate)
            ORDER BY t.creationDate DESC
            """)
    List<Ticket> search(@Param("ticketId") Long ticketId,
                        @Param("userId") Long userId,
                        @Param("techId") Long techId,
                        @Param("status") Status status,
                        @Param("priority") Priority priority,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
    
    // How much tickets submitted in a period
    long countByCreationDateBetween(LocalDateTime from, LocalDateTime to);
 
    // How much tickets resolved in a period
    long countByResolutionDateBetween(LocalDateTime from, LocalDateTime to);
    
    List<Ticket> findByResolutionDateBetween(LocalDateTime from, LocalDateTime to);
 
    // Open Tickets: pass List.of(Status.RESOLVED, Status.CLOSED)
    List<Ticket> findByStatusNotIn(Collection<Status> statuses);
 
    long countByStatusNotIn(Collection<Status> statuses);
 
    long countByStatus(Status status);
 
    // Technician Workload
    // Pass List.of(Status.ASSIGNED, Status.IN_PROGRESS, Status.PENDING)
    @Query("""
            SELECT tech.id, tech.name, COUNT(t)
            FROM Ticket t JOIN t.technician tech
            WHERE t.status IN :statuses
            GROUP BY tech.id, tech.name
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> technicianWorkload(@Param("statuses") Collection<Status> statuses);
 
    // Average Resolution Time in hours (null if nothing has been resolved yet)
    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(MINUTE, creation_date, resolution_date)) / 60.0
            FROM tickets
            WHERE resolution_date IS NOT NULL
            """, nativeQuery = true)
    Double averageResolutionHours();
}