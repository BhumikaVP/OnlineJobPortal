package com.jobportal.service;

import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Application;
import com.jobportal.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    public Application applyForJob(Integer userId, Integer jobId) {
        Optional<Application> existing = applicationRepository.findByUserIdAndJobId(userId, jobId);
        if (existing.isPresent()) {
            throw new RuntimeException("Already applied for this job");
        }
        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(jobId);
        application.setStatus("Pending");
        return applicationRepository.save(application);
    }

    public List<Application> getApplicationsByUser(Integer userId) {
        return applicationRepository.findByUserId(userId);
    }

    public List<Application> getApplicationsForJob(Integer jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public Application updateApplicationStatus(Integer id, String status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        application.setStatus(status);
        return applicationRepository.save(application);
    }
}
