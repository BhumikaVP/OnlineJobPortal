package com.jobportal.controller;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Integer id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        if (!"Recruiter".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only recruiters can post jobs");
        }
        job.setRecruiterId(user.getId());
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Integer id, @RequestBody Job jobDetails, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        Job existingJob = jobService.getJobById(id);
        
        if (!user.getId().equals(existingJob.getRecruiterId()) && !"Admin".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Not authorized to update this job");
        }
        return ResponseEntity.ok(jobService.updateJob(id, jobDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Integer id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        Job existingJob = jobService.getJobById(id);
        
        if (!user.getId().equals(existingJob.getRecruiterId()) && !"Admin".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Not authorized to delete this job");
        }
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }
}
