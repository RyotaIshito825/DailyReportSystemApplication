package com.techacademy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.ProfileImage;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportService reportService;
    private final ProfileImageService profileImageService;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder,
            ReportService reportService, ProfileImageService profileImageService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
        this.profileImageService = profileImageService;
    }

    // アップロード先のディレクトリ
    private static final String UPLOAD_DIR = "/Users/ryotaishito/DailyReportSystemApplicationImages";
    private static final String LOCAL_HOST = "http://localhost:8080";
    private static final String PROFILE_IMAGES_DIR = "profileImages";
    private static final String NOIMAGE_FILE_PATH = "../../img/profile-noimage.png";

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {
        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }
        // 従業員メール重複チェック
        if (findByEmail(employee.getEmail()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }
        return ErrorKinds.SUCCESS;
    }

    // 従業員の保存 （後にプロフィール画像を保存するため切り分けた)
    public void saveEmployee(Employee employee) {

        employee.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        if (employee.getCode() != null) {
            Employee emp = findByCode(employee.getCode());
            employee.setCreatedAt(emp.getCreatedAt());
            if (employee.getPassword().equals("")) {
                employee.setPassword(emp.getPassword());
            } else {
                employee.setPassword(employee.getPassword());
            }
        } else {
            employee.setCreatedAt(now);
        }

        employee.setUpdatedAt(now);
        employee.setName(employee.getName());
        employee.setRole(employee.getRole());
        employee.setEmail(employee.getEmail());

        employeeRepository.save(employee);
    }

    // 従業員更新
    @Transactional
    public ErrorKinds updateEmployee(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employee.getPassword().equals("") ? ErrorKinds.CHECK_OK : employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員メール重複チェック
        Employee nowEmployee = findByCode(employee.getCode());
        if (findByEmail(employee.getEmail()) != null && !nowEmployee.getEmail().equals(employee.getEmail())) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        // メール入力チェック
        if (employee.getEmail().equals("")) {
            return ErrorKinds.EMAIL_BLANK_ERROR;
        }

        return ErrorKinds.SUCCESS;
    }

    public void employeeUpdate(Employee employee) {
        Employee emp = findByCode(employee.getCode());

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(emp.getCreatedAt());
        employee.setUpdatedAt(now);
        employee.setName(employee.getName());

        if (employee.getPassword().equals("")) {
            employee.setPassword(emp.getPassword());
        } else {
            employee.setPassword(employee.getPassword());
        }
        employee.setRole(employee.getRole());
        employee.setEmail(employee.getEmail());

        employeeRepository.save(employee);
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(Integer code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        List<Report> reports = reportService.findByEmployee(employee);
        for (Report r : reports) {
            reportService.delete(r.getId());
        }

//        try {
//            ProfileImage profileImage = profileImageService.findByCode(employee);
//            if (profileImageService.isFileExists(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR + File.separator + profileImage)) {
//                Files.delete(Paths.get(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR + File.separator + profileImage));
//            }
//        } catch (IOException e) {
//            return ErrorKinds.FILETYPE_ERROR;
//        }

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(Integer code) {
//        if (code == null) {
//            return new Employee();
//        }
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // メールで従業員検索
    public Employee findByEmail(String email) {
        Optional<Employee> option = employeeRepository.findByEmail(email);
        Employee employee = option.orElse(null);

        return employee;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

    // ファイルの削除
    public void deleteFile(Integer code) {
        try {
            Employee employee = findByCode(code);
            ProfileImage profileImage = profileImageService.findByCode(employee);
            if (profileImage != null) {
                System.out.println(profileImage.getFilePath());
                if (profileImageService.isFileExists(UPLOAD_DIR + profileImage.getFilePath())) {
                    Files.delete(Paths.get(UPLOAD_DIR + profileImage.getFilePath()));
                    System.out.println("ファイルを削除しました");
                } else {
                    System.out.println("ファイルを削除できませんでした");
                }
            }
            System.out.println("profileImage is null");
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    // 指定ディレクトリ内のファイルをリストで取得
    public List<Path> getFilePaths(String dirName) {
        Path directoryPath = Paths.get(dirName);
        List<Path> paths = new ArrayList<>();
        try {
            Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .forEach(paths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }
    // リストから指定したファイル名のファイルを削除
    public void deleteFile(List<Path> paths, String fileName) {
        try {
            for (Path path : paths) {
                if (path.toString().contains(fileName)) {
                    Files.delete(path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
