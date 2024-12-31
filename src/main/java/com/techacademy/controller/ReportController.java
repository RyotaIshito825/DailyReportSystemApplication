package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    public String list(Model model) {
        model.addAttribute("listSize", reportService.findAll().size());
        model.addAttribute("reportList", reportService.findAll());
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




}
