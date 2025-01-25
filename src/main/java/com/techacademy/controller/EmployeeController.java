package com.techacademy.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.ProfileImage;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ProfileImageService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ReportService reportService;
    private final ProfileImageService profileImageService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, ReportService reportService, ProfileImageService profileImageService) {
        this.employeeService = employeeService;
        this.reportService = reportService;
        this.profileImageService = profileImageService;
    }

//    @ModelAttribute("employee")
//    public Employee setUpEmployee() {
//        // セッションに保存するための Employee オブジェクトを初期化
//        return new Employee();
//    }

    // アップロード先のディレクトリ
    private static final String UPLOAD_DIR = "/Users/ryotaishito/DailyReportSystemApplicationImages";
    private static final String LOCAL_HOST = "http://localhost:8080";
    private static final String PROFILE_IMAGES_DIR = "profileImages";
    private static final String NOIMAGE_FILE_PATH = "../../img/profile-noimage.png";

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {
        //プロフィール画像の取得
        List<Employee> employeeList = employeeService.findAll();
        // getProfileImageMapで対応付いたMapを取得してmodelへ
        model.addAttribute("profileImageMap", profileImageService.getProfileImageMap(employeeList));
        model.addAttribute("listSize", employeeList.size());
        model.addAttribute("employeeList", employeeList);

        return "employees/list";
    }

    // パスワード再設定画面表示
//    @GetMapping(value = "/password_reset")
//    public String passwordreset() {
//        return "login/passwordreset";
//    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable Integer code, Model model) {

        Employee employee = employeeService.findByCode(code);
        System.out.println("profileImage : " + profileImageService.getProfileImagePath(employee));

        model.addAttribute("profileImage", profileImageService.getProfileImagePath(employee));
        model.addAttribute("employee", employee);
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, MultipartFile file, Model model) {
        // パスワード空白チェック
        /*
         * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白でもチェックエラーを出さずに
         * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出す方法が簡単だと判断
         */
        if ("".equals(employee.getPassword())) {
            // パスワードが空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            return create(employee);
        }

        // 入力チェック
        if (res.hasErrors()) {
            System.out.println("hasErrors() error");
            return create(employee);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

            ProfileImage profileImage = new ProfileImage();
            // フォルダに保存する画像ファイルの名前を変更
            String newFileName = "profile-image_" + employee.getCode() + profileImageService.getFileExtension(file.getOriginalFilename());
            // DB用の画像ファイルの名前
            String dbProfileFileName = "profile-image_" + employee.getCode();
            // 画像ファイルの保存先パス
            String filePath = UPLOAD_DIR + File.separator + "profileImages" + File.separator + newFileName;
            // ファイルの保存先
            Path destination = new File(filePath).toPath();
            if (!file.isEmpty()) {
               profileImage.setFilePath(File.separator + PROFILE_IMAGES_DIR + File.separator + newFileName);
            } else {
               profileImage.setFilePath(NOIMAGE_FILE_PATH);
            }

            profileImage.setName(dbProfileFileName);
            profileImage.setFileSize(file.getSize());
            profileImage.setEmployee(employee);
            profileImage.setFileType(file.getContentType());

            ErrorKinds proFileImageresult = profileImageService.save(profileImage, employee);

            if (ErrorMessage.contains(proFileImageresult)) {
               model.addAttribute(ErrorMessage.getErrorName(proFileImageresult), ErrorMessage.getErrorValue(proFileImageresult));
               return create(employee);
            }

            if (result == ErrorKinds.SUCCESS && proFileImageresult == ErrorKinds.SUCCESS) {
               employeeService.saveEmployee(employee);

               profileImage.setEmployee(employee);
               dbProfileFileName = "profile-image_" + employee.getCode();
               newFileName = "profile-image_" + employee.getCode() + profileImageService.getFileExtension(file.getOriginalFilename());
               filePath = UPLOAD_DIR + File.separator + "profileImages" + File.separator + newFileName;
               profileImage.setName(dbProfileFileName);
               profileImage.setFileSize(file.getSize());
               profileImage.setFileType(file.getContentType());

               if (!file.isEmpty()) {
                   profileImage.setFilePath(File.separator + PROFILE_IMAGES_DIR + File.separator + newFileName);
               } else {
                   profileImage.setFilePath("../../img/profile-noimage.png");
               }

               profileImageService.saveProfileImage(profileImage, employee);

               // 最後に写真を保存する
               if (!file.isEmpty()) {
                   // フォルダがない場合は作成する
                   if (!profileImageService.isDirectoryExists(UPLOAD_DIR)) {
                       Files.createDirectories(Paths.get(UPLOAD_DIR));
                   };
                   if (!profileImageService.isDirectoryExists(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR)) {
                       Files.createDirectories(Paths.get(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR));
                   }

                   // ディスクに保存する前に保存する前に同名のファイル削除
                   List<Path> paths = employeeService.getFilePaths(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR + File.separator);
                   employeeService.deleteFile(paths, profileImage.getName());
                   // 画像ファイルをディスクに保存
                   destination = new File(filePath).toPath();
                   Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
               }
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        } catch (IOException e) {
            return create(employee);
        }

        return "redirect:/employees";
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable Integer code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        // ディスクに保存する前に保存する前に同名のファイル削除
        List<Path> paths = employeeService.getFilePaths(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR + File.separator);
        Employee employee = employeeService.findByCode(code);
        ProfileImage profileImage = profileImageService.findByCode(employee);
        employeeService.deleteFile(paths, profileImage.getName());

        ErrorKinds result = employeeService.delete(code, userDetail);
        // ファイルの削除
        employeeService.deleteFile(code);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

    // 従業員更新処理画面
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable Integer code, Model model) {

        Employee employee = employeeService.findByCode(code);
        model.addAttribute("profileImage", profileImageService.getProfileImagePath(employee));
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/edit";
    }

    // 従業員更新処理
    @PostMapping(value = "/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, MultipartFile file, Model model) {
        ErrorKinds result = employeeService.updateEmployee(employee);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("profileImage", "../../img/profile-noimage.png");
            return "employees/edit";
        }

        if (res.hasErrors()) {
            model.addAttribute("employee", employee);
            model.addAttribute("profileImage", profileImageService.getProfileImagePath(employee));
            return "employees/edit";
        }

        // プロフィール画像の保存
        try {
            // プロフィール画像がなかったら、新規で登録
            ProfileImage profileImage = profileImageService.findByCode(employee);
            if (profileImage == null) {
                profileImage = new ProfileImage();
                LocalDateTime now = LocalDateTime.now();
                profileImage.setFileSize(file.getSize());
                profileImage.setFileType(file.getContentType());
                profileImage.setEmployee(employee);
                profileImage.setCreatedAt(now);
                profileImage.setUpdatedAt(now);
            }

            // フォルダに保存する画像ファイルの名前を変更
            String newFileName = "profile-image_" + employee.getCode() + profileImageService.getFileExtension(file.getOriginalFilename());
            // DB用の画像ファイルの名前
            String dbProfileFileName = "profile-image_" + employee.getCode();
            // 画像ファイルの保存先パス
            String filePath = UPLOAD_DIR + File.separator + "profileImages" + File.separator + newFileName;
            // 画像の保存先
            Path destination = new File(filePath).toPath();
            if (!file.isEmpty()) {
                profileImage.setFilePath(File.separator + PROFILE_IMAGES_DIR + File.separator + newFileName);
            } else {
                profileImage.setFilePath("../../img/profile-noimage.png");
            }

            profileImage.setName(dbProfileFileName);
            profileImage.setFileSize(file.getSize());
            profileImage.setFileType(file.getContentType());

            ErrorKinds profileImageResult = profileImageService.updateProfileImage(profileImage);


            if (ErrorMessage.contains(profileImageResult)) {
                model.addAttribute(ErrorMessage.getErrorName(profileImageResult), ErrorMessage.getErrorValue(profileImageResult));
                profileImage.setFilePath("../../img/profile-noimage.png");
                model.addAttribute("profileImage", profileImage.getFilePath());
                return "employees/edit";
            }

            if (result == ErrorKinds.SUCCESS && profileImageResult == ErrorKinds.SUCCESS) {
                employeeService.saveEmployee(employee);

                profileImage.setName(dbProfileFileName);
                profileImage.setFileSize(file.getSize());
                profileImage.setFileType(file.getContentType());

                if (!file.isEmpty()) {
                    profileImage.setFilePath(File.separator + PROFILE_IMAGES_DIR + File.separator + newFileName);
                } else {
                    profileImage.setFilePath("../../img/profile-noimage.png");
                }

                profileImageService.profileImageUpdate(profileImage);

                // 最後にファイルを保存する
                if (!file.isEmpty()) {
                    // フォルダがない場合は作成する
                    if (!profileImageService.isDirectoryExists(UPLOAD_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR));
                    }
                    if (!profileImageService.isDirectoryExists(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR)) {
                        Files.createDirectories(Paths.get(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR));
                    }

                    // ディスクに保存する前に同名のファイル削除
                    List<Path> paths = employeeService.getFilePaths(UPLOAD_DIR + File.separator + PROFILE_IMAGES_DIR + File.separator);
                    employeeService.deleteFile(paths, profileImage.getName());
                    // 画像ファイルをディスクに保存
                    destination = new File(filePath).toPath();
                    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
             }

        } catch (IOException e) {
            return "employees/edit";
        }
        return "redirect:/employees";
    }
}
