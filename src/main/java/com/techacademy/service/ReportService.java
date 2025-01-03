package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
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

    // 削除対象の従業員に紐づいている日報リスト取得
    public List<Report> findByEmployee(Employee employee) {
        return reportRepository.findByEmployee(employee);
    }

    // 日報1件を検索
    public Report findById(Integer id) {
        Optional<Report> option = reportRepository.findById(id);
        Report report = option.orElse(null);
        return report;
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
        report.setContent(report.getContent());
        report.setDeleteFlg(false);
        report.setReportDate(report.getReportDate());
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds updateReport(Employee employee, Report report) {
        Report rep = findById(report.getId());
        ErrorKinds result = isReportDateCheck(employee, report);

        // 表示中の日報データの日付はOK
        if (report.getReportDate().equals(rep.getReportDate())) {
            result = ErrorKinds.CHECK_OK;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setTitle(report.getTitle());
        report.setContent(report.getContent());
        report.setReportDate(report.getReportDate());
        report.setCreatedAt(rep.getCreatedAt());
        report.setUpdatedAt(now);

        reportRepository.save(report);

        if (ErrorKinds.CHECK_OK != result) {
            return result;
        } else {
            return ErrorKinds.SUCCESS;
        }
    }

    // 日報日付チェック（新規登録時）
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

    // 日報日付チェック（更新時）
    public ErrorKinds isReportDateCheck(Employee employee, Report report) {
        List<Report> reports = reportRepository.findByReportDate(report.getReportDate());
        if (!isRegisteredDateCheck(employee, report)) {
            return ErrorKinds.CHECK_OK;
        } else if (!isDisplayingEmployeeCheck(employee, reports)) {
            return ErrorKinds.CHECK_OK;
        }
        return ErrorKinds.DATECHECK_ERROR;
    }
    // 日付チェック（更新時）
    public boolean isRegisteredDateCheck(Employee employee, Report report) {
        List<Report> reports = reportRepository.findByReportDate(report.getReportDate());

        boolean isExistReportDates = reports.stream()
                .filter(r -> !r.equals(report))
                .anyMatch(r -> r.getReportDate().equals(report.getReportDate()));

        return isExistReportDates;
    }
    // レポートに画面で表示中の従業員のものが含まれているかチェック
    public boolean isDisplayingEmployeeCheck(Employee employee, List<Report> reports) {
        boolean isExist = reports.stream().anyMatch(r -> r.getEmployee().getCode().equals(employee.getCode()));
        return isExist;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(Integer id) {
        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }
}
