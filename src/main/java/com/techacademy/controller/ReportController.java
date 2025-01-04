package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;

    @Autowired
    public ReportController(ReportService reportService, EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {

        Employee employee = employeeService.findByCode(userDetail.getEmployee().getCode());
        // ログイン中の従業員の権限で分岐
        if (employee.getRole() == Employee.Role.ADMIN) {
            model.addAttribute("listSize", reportService.findAll().size());
            model.addAttribute("reportList", reportService.findAll());
        } else {
            model.addAttribute("listSize", reportService.findByEmployee(employee).size());
            model.addAttribute("reportList", reportService.findByEmployee(employee));
        }

        return "reports/list";
    }

    //日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@AuthenticationPrincipal UserDetail userDetail, @ModelAttribute Report report, Model model) {
        Employee employee = employeeService.findByCode(userDetail.getUsername());
        report.setEmployee(employee);
        model.addAttribute("report", report);
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@AuthenticationPrincipal UserDetail userDetail, @Validated Report report, BindingResult res, Model model) {
        Employee employee = employeeService.findByCode(userDetail.getUsername());
        report.setEmployee(employee);

        if (res.hasErrors()) {
            model.addAttribute("report", report);
            return "reports/new";
        }

        ErrorKinds result = reportService.save(userDetail, report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "reports/new";
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }

    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable Integer id, Model model) {

        Employee employee = employeeService.findByCode(reportService.findById(id).getEmployee().getCode());
        Report report = reportService.findById(id);
        report.setEmployee(employee);
        model.addAttribute("report", reportService.findById(id));
        return "reports/edit";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, BindingResult res, Model model) {
        Employee employee = employeeService.findByCode(reportService.findById(report.getId()).getEmployee().getCode());
        report.setEmployee(employee);
        if (res.hasErrors()) {
            model.addAttribute("report", report);
            return "reports/edit";
        }

        ErrorKinds result = reportService.updateReport(employee, report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "reports/edit";
        }
        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, Model model) {
        ErrorKinds result = reportService.delete(id);
        if (ErrorMessage.contains(result)) {
            model.addAttribute("report", reportService.findById(id));
            return "reports/detail";
        }
        return "redirect:/reports";
    }
}
