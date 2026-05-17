package com.jobportal.controller;

import com.jobportal.model.Application;
import com.jobportal.model.User;
import com.jobportal.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/{jobId}")
    public ResponseEntity<?> applyForJob(@PathVariable Integer jobId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        if (!"Job Seeker".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only Job Seekers can apply for jobs");
        }
        try {
            Application application = applicationService.applyForJob(user.getId(), jobId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        return ResponseEntity.ok(applicationService.getApplicationsByUser(user.getId()));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicationsForJob(@PathVariable Integer jobId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        if (!"Recruiter".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only recruiters can view job applications");
        }
        // In a real app, verify that the user is the recruiter for THIS job.
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Integer id, @RequestBody Application statusUpdate, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        if (!"Recruiter".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only recruiters can update application status");
        }
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, statusUpdate.getStatus()));
    }
}
