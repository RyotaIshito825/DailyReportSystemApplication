package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    // 対象の日付のレポートをリストで返す
    List<Report> findByReportDate(LocalDate reportDate);
}
