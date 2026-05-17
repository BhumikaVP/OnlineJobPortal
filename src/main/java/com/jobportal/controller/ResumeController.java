package com.jobportal.controller;

import com.jobportal.model.Resume;
import com.jobportal.model.User;
import com.jobportal.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) session.getAttribute("user");
        if (!"Job Seeker".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only Job Seekers can upload resumes");
        }

        try {
            Resume resume = resumeService.uploadResume(user.getId(), file);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload resume: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getResume(@PathVariable Integer userId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        // In a real app, recruiters could view this. For MVP, we'll allow logged-in users.
        try {
            Resume resume = resumeService.getResumeByUser(userId);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
