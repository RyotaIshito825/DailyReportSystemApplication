package com.techacademy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;
import com.techacademy.entity.ReportFile;

public interface ReportFileRepository extends JpaRepository<ReportFile, Integer> {
    Optional<ReportFile> findByReport(Report report);
}
