package com.jobportal.config;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed data if no users exist
        if (userRepository.count() == 0) {
            System.out.println("Seeding initial data...");

            // Create Admin
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@jobportal.com");
            admin.setPassword("admin123");
            admin.setRole("Admin");
            userRepository.save(admin);

            // Create Recruiter
            User recruiter = new User();
            recruiter.setName("Tech Recruiter");
            recruiter.setEmail("recruiter@tech.com");
            recruiter.setPassword("recruiter123");
            recruiter.setRole("Recruiter");
            userRepository.save(recruiter);

            // Create Job Seeker
            User seeker = new User();
            seeker.setName("John Doe");
            seeker.setEmail("john@example.com");
            seeker.setPassword("seeker123");
            seeker.setRole("Job Seeker");
            userRepository.save(seeker);

            // Create Jobs
            Job job1 = new Job();
            job1.setTitle("Software Engineer");
            job1.setCompany("Tech Innovators Inc.");
            job1.setLocation("San Francisco, CA");
            job1.setDescription("We are looking for a skilled Software Engineer with experience in Java and Spring Boot to join our core backend team.");
            job1.setRecruiterId(recruiter.getId());
            jobRepository.save(job1);

            Job job2 = new Job();
            job2.setTitle("Frontend Developer");
            job2.setCompany("Creative Web Solutions");
            job2.setLocation("Remote");
            job2.setDescription("Seeking a creative Frontend Developer proficient in React, HTML, CSS, and modern JavaScript frameworks.");
            job2.setRecruiterId(recruiter.getId());
            jobRepository.save(job2);

            Job job3 = new Job();
            job3.setTitle("Data Scientist");
            job3.setCompany("Data Driven LLC");
            job3.setLocation("New York, NY");
            job3.setDescription("Join our analytics team to build predictive models using Python, TensorFlow, and advanced machine learning techniques.");
            job3.setRecruiterId(recruiter.getId());
            jobRepository.save(job3);

            System.out.println("Data seeding completed.");
        }
    }
}
