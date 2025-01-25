package com.techacademy.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.entity.ReportFile;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportFileService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;
    private final ReportFileService reportFileService;

    @Autowired
    public ReportController(ReportService reportService, EmployeeService employeeService, ReportFileService reportFileService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
        this.reportFileService = reportFileService;
    }

    // アップロード先のディレクトリ
    private static final String UPLOAD_DIR = "/Users/ryotaishito/DailyReportSystemApplicationImages";
    private static final String LOCAL_HOST = "http://localhost:8080";
    private static final String REPORTFILES_DIR = "reportFiles";
    private static final String NOIMAGE_FILE_PATH = "../../img/report-nofile.png";

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {

        Employee employee = employeeService.findByCode(userDetail.getEmployee().getCode());
        Map<Integer, String> reportFileMap = reportFileService.getReportFileMap(reportService.findAll());
        // ログイン中の従業員の権限で分岐
        if (employee.getRole() == Employee.Role.ADMIN) {
            model.addAttribute("listSize", reportService.findAll().size());
            model.addAttribute("reportList", reportService.findAll());
            model.addAttribute("reportFileMap", reportFileMap);
        } else {
            model.addAttribute("listSize", reportService.findByEmployee(employee).size());
            model.addAttribute("reportList", reportService.findByEmployee(employee));
            model.addAttribute("reportFileMap", reportFileMap);
        }

        return "reports/list";
    }

    //日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@AuthenticationPrincipal UserDetail userDetail, @ModelAttribute Report report, Model model) {
        Employee employee = employeeService.findByCode(Integer.parseInt(userDetail.getUsername()));
        report.setEmployee(employee);
        model.addAttribute("report", report);
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@AuthenticationPrincipal UserDetail userDetail, @Validated Report report, BindingResult res, MultipartFile file, Model model) {
        Employee employee = employeeService.findByCode(Integer.parseInt(userDetail.getUsername()));
        report.setEmployee(employee);

        if (res.hasErrors()) {
            model.addAttribute("report", report);
            return "reports/new";
        }

        try {
            ErrorKinds result = reportService.save(userDetail, report);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return "reports/new";
            }

            ReportFile reportFile = new ReportFile();
            // フォルダに保存する画像ファイルの名前を変更
            String newFileName = "report-file_" + employee.getCode() + reportFileService.getFileExtension(file.getOriginalFilename());
            // DB用の画像ファイルの名前
            String dbReportFileName = "report-file_" + employee.getCode();
            // 画像ファイルの保存先パス
            String filePath = UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + newFileName;
            // ファイルの保存先
            Path destination = new File(filePath).toPath();
            if (!file.isEmpty()) {
                reportFile.setFilePath(File.separator + REPORTFILES_DIR + File.separator + newFileName);
            } else {
                reportFile.setFilePath(NOIMAGE_FILE_PATH);
            }

            reportFile.setName(dbReportFileName);
            reportFile.setFileSize(file.getSize());
            reportFile.setReport(report);
            reportFile.setFileType(file.getContentType());

            ErrorKinds reportFileresult = reportFileService.save(reportFile, report);

            System.out.println("エラーあり");
            if (ErrorMessage.contains(reportFileresult)) {
                model.addAttribute(ErrorMessage.getErrorName(reportFileresult), ErrorMessage.getErrorValue(reportFileresult));
                return create(userDetail, report, model);
             }
            System.out.println("エラーなし");

            if (result == ErrorKinds.SUCCESS && reportFileresult == ErrorKinds.SUCCESS) {

                reportService.saveReport(report);

                reportFile.setReport(report);
                dbReportFileName = "report-file_" + report.getId();
                newFileName = "report-file_" + report.getId() + reportFileService.getFileExtension(file.getOriginalFilename());
                filePath = UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + newFileName;
                reportFile.setName(dbReportFileName);
                reportFile.setFileSize(file.getSize());
                reportFile.setFileType(file.getContentType());

                if (!file.isEmpty()) {
                    reportFile.setFilePath(File.separator + REPORTFILES_DIR + File.separator + newFileName);
                } else {
                    reportFile.setFilePath(NOIMAGE_FILE_PATH);
                }

                System.out.println("reportFileService.saveReportFile(reportFile, report) before");
                reportFileService.saveReportFile(reportFile, report);
                System.out.println("reportFileService.saveReportFile(reportFile, report) after");

                // 最後にファイルを保存する
                if (!file.isEmpty()) {
                    // フォルダがない場合は作成する
                    if (!reportFileService.isDirectoryExists(UPLOAD_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR));
                    };
                    if (!reportFileService.isDirectoryExists(UPLOAD_DIR + File.separator + REPORTFILES_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR + File.separator + REPORTFILES_DIR));
                    }

                    // ディスクに保存する前に同名のファイル削除
                    List<Path> paths = reportService.getFilePaths(UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator);
                    reportService.deleteFile(paths, reportFile.getName());
                    // ファイルをディスクに保存
                    destination = new File(filePath).toPath();
                    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(userDetail, report, model);
        } catch (IOException e) {
            return create(userDetail, report, model);
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {

        Report report = reportService.findById(id);
        model.addAttribute("report", report);
        model.addAttribute("reportFile", reportFileService.getReportFilePath(report));

        ReportFile reportFile = reportFileService.findByReport(report);
        if (reportFile != null) {
            String filePath = UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + reportFile.getName() + reportFileService.getFileExtension(reportFile.getFilePath());
            if (!reportFileService.isFileExists(filePath)) {
                model.addAttribute("reportFile", NOIMAGE_FILE_PATH);
            }
        }
        return "reports/detail";
    }

    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model) {

        Employee employee = employeeService.findByCode(reportService.findById(id).getEmployee().getCode());
        Report report = reportService.findById(id);
        report.setEmployee(employee);
        String reportFilePath = reportFileService.getReportFilePath(report);
        if (reportFilePath.isEmpty()) {
            model.addAttribute("reportFile", NOIMAGE_FILE_PATH);
        } else {
            model.addAttribute("reportFile", reportFileService.getReportFilePath(report));
        }

        ReportFile reportFile = reportFileService.findByReport(report);
        if (reportFile != null) {
            String filePath = UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + reportFile.getName() + reportFileService.getFileExtension(reportFile.getFilePath());
            if (!reportFileService.isFileExists(filePath)) {
                model.addAttribute("reportFile", NOIMAGE_FILE_PATH);
            }
        }

        model.addAttribute("report", reportService.findById(id));
        return "reports/edit";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, BindingResult res, MultipartFile file, Model model) {
        Employee employee = employeeService.findByCode(reportService.findById(report.getId()).getEmployee().getCode());
        report.setEmployee(employee);

        if (res.hasErrors()) {
            model.addAttribute("report", report);
            model.addAttribute("reportFile", NOIMAGE_FILE_PATH);
            return "reports/edit";
        }

        ErrorKinds result = reportService.updateReport(employee, report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            System.out.println("error2");
            return "reports/edit";
        }

        // レポートファイルの保存
        try {
            // レポートファイルがなかったら、新規で登録
            ReportFile reportFile = reportFileService.findByReport(report);
            if (reportFile == null) {
                reportFile = new ReportFile();
                LocalDateTime now = LocalDateTime.now();
                reportFile.setFileSize(file.getSize());
                reportFile.setFileType(file.getContentType());
                reportFile.setReport(report);
                reportFile.setCreatedAt(now);
                reportFile.setUpdatedAt(now);
            }

            // フォルダに保存するファイルの名前の変更
            String newFileName = "report-file_" + report.getId() + reportFileService.getFileExtension(file.getOriginalFilename());
            // DB用のファイルの名前
            String dbReportFileName = "report-file_" + report.getId();
            // ファイルの保存先パス
            String filePath = UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + newFileName;
            // ファイルの保存先
            Path destination = new File(filePath).toPath();
            if (!file.isEmpty()) {
                reportFile.setFilePath(File.separator + REPORTFILES_DIR + File.separator + newFileName);
            } else {
                reportFile.setFilePath(NOIMAGE_FILE_PATH);
            }

            reportFile.setName(dbReportFileName);
            reportFile.setFileSize(file.getSize());
            reportFile.setFileType(file.getContentType());

            ErrorKinds reportFileResult = reportFileService.updateReportFile(reportFile);


            if (ErrorMessage.contains(reportFileResult)) {
                model.addAttribute(ErrorMessage.getErrorName(reportFileResult), ErrorMessage.getErrorValue(reportFileResult));
                reportFile.setFilePath(NOIMAGE_FILE_PATH);
                model.addAttribute("reportFile", reportFile.getFilePath());
                return "reports/edit";
            }

            if (result == ErrorKinds.SUCCESS && reportFileResult == ErrorKinds.SUCCESS) {
                reportService.saveReport(report);

                reportFile.setName(dbReportFileName);
                reportFile.setFileSize(file.getSize());
                reportFile.setFileType(file.getContentType());

                if (!file.isEmpty()) {
                    reportFile.setFilePath(File.separator + REPORTFILES_DIR + File.separator + newFileName);
                } else {
                    reportFile.setFilePath(NOIMAGE_FILE_PATH);
                }

                reportFileService.reportFileUpdate(reportFile);

                // 最後にファイルを保存する
                if (!file.isEmpty()) {
                    // フォルダがない場合は作成する
                    if (!reportFileService.isDirectoryExists(UPLOAD_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR));
                    }
                    if (!reportFileService.isDirectoryExists(UPLOAD_DIR + File.separator + REPORTFILES_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR + File.separator + REPORTFILES_DIR));
                    }

                    // ディスクに保存する前に同名のファイル削除
                    List<Path> paths = reportService.getFilePaths(UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator);
                    reportService.deleteFile(paths, reportFile.getName());
                    // ファイルをディスクに保存
                    destination = new File(filePath).toPath();
                    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            return "reports/edit";
        }
        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, Model model) {

        List<Path> paths = reportService.getFilePaths(UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator);
        Report report = reportService.findById(id);
        ReportFile reportFile = reportFileService.findByReport(report);
        reportService.deleteFile(paths, reportFile.getName());

        ErrorKinds result = reportService.delete(id);
        // ファイルの削除
        reportService.deleteFile(id);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return detail(id, model);
//            return "reports/detail";
        }
        return "redirect:/reports";
    }
}
