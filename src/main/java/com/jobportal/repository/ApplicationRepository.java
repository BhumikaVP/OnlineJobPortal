package com.jobportal.repository;

import com.jobportal.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByUserId(Integer userId);
    List<Application> findByJobId(Integer jobId);
    Optional<Application> findByUserIdAndJobId(Integer userId, Integer jobId);
}
