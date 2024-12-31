package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 日報登録
    @Transactional
    public ErrorKinds save(@AuthenticationPrincipal UserDetail userDetail, Report report) {
        ErrorKinds result = isRegisteredDateCheck(userDetail, report);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setTitle(report.getTitle());
        report.setContent(report.getTitle());
        report.setDeleteFlg(false);
        report.setReportDate(report.getReportDate());
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報日付チェック
    public ErrorKinds isRegisteredDateCheck(@AuthenticationPrincipal UserDetail userDetail, Report report) {
        List<Report> reports = reportRepository.findByReportDate(report.getReportDate());
        if (!isLoginEmployeeCheck(userDetail, reports)) {
            return ErrorKinds.CHECK_OK;
        }
        return ErrorKinds.DATECHECK_ERROR;
    }

    // レポートにログイン中の従業員のものが含まれているかチェック
    public boolean isLoginEmployeeCheck(@AuthenticationPrincipal UserDetail userDetail, List<Report> reports) {
        boolean isExist = reports.stream().anyMatch(r -> r.getEmployee().getCode().equals(userDetail.getUsername()));
        return isExist;
    }
}
