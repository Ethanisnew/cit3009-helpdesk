package com.university.helpdesk.controller;

import com.university.helpdesk.dto.TechnicianResponse;
import com.university.helpdesk.entity.model.Technician;
import com.university.helpdesk.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

    @Autowired
    private TechnicianRepository technicianRepository;

    @GetMapping
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {
        List<TechnicianResponse> techs = technicianRepository.findAll().stream()
                .map(TechnicianResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(techs);
    }

    @PostMapping
    public ResponseEntity<TechnicianResponse> createTechnician(@RequestBody Map<String, String> body) {
        Technician tech = new Technician(body.get("username"), body.get("password"), body.get("fullName"), body.get("specialization"));
        return ResponseEntity.ok(new TechnicianResponse(technicianRepository.save(tech)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponse> updateTechnician(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Technician tech = technicianRepository.findById(id).orElseThrow(() -> new RuntimeException("Tech not found"));
        tech.setName(body.get("fullName"));
        tech.setSpecialization(body.get("specialization"));
        return ResponseEntity.ok(new TechnicianResponse(technicianRepository.save(tech)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        technicianRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}