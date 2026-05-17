package com.jobportal.service;

import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Job;
import com.jobportal.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job(1, "Software Engineer", "Tech Corp", "Remote", "Java Dev", 100);
    }

    @Test
    void testCreateJob() {
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        Job savedJob = jobService.createJob(testJob);

        assertNotNull(savedJob);
        assertEquals("Software Engineer", savedJob.getTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void testGetAllJobs() {
        when(jobRepository.findAll()).thenReturn(Arrays.asList(testJob));

        List<Job> jobs = jobService.getAllJobs();

        assertFalse(jobs.isEmpty());
        assertEquals(1, jobs.size());
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    void testGetJobByIdSuccess() {
        when(jobRepository.findById(1)).thenReturn(Optional.of(testJob));

        Job foundJob = jobService.getJobById(1);

        assertNotNull(foundJob);
        assertEquals("Tech Corp", foundJob.getCompany());
    }

    @Test
    void testGetJobByIdNotFound() {
        when(jobRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            jobService.getJobById(1);
        });
    }

    @Test
    void testDeleteJob() {
        doNothing().when(jobRepository).deleteById(1);

        jobService.deleteJob(1);

        verify(jobRepository, times(1)).deleteById(1);
    }
}
