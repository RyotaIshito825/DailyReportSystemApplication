package com.techacademy.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.ProfileImage;
import com.techacademy.repository.ProfileImageRepository;

@Service
public class ProfileImageService {
    private final ProfileImageRepository profileImageRepository;
    // アップロード先のディレクトリ
    private static final String UPLOAD_DIR = "/Users/ryotaishito/DailyReportSystemApplicationImages";
    private static final String LOCAL_HOST = "http://localhost:8080";
    private static final String PROFILE_IMAGES_DIR = "profileImages";
    private static final String NOIMAGE_FILE_PATH = "../../img/profile-noimage.png";

    public ProfileImageService(ProfileImageRepository profileImageRepository) {
        this.profileImageRepository = profileImageRepository;
    }

    // ファイルの最大サイズ(1MB)
    private static final long MAX_FILE_SIZE = 1024 * 1024;

    // プロフィール画像保存
    @Transactional
    public ErrorKinds save(ProfileImage profileImage, Employee employee) {

        // ファイルサイズチェック
        if (profileImage.getFileSize() > MAX_FILE_SIZE) {
            return ErrorKinds.FILESIZE_ERROR;
        }
        // ファイルタイプチェック
        String contentType = profileImage.getFileType();
        if (!profileImage.getFilePath().equals(NOIMAGE_FILE_PATH)) {
            if (!contentType.startsWith("image/")) {
                return ErrorKinds.FILETYPE_ERROR;
            }
        }
        return ErrorKinds.SUCCESS;
    }

    // プロフィール画像の保存 （先に従業員を保存するために切り分け)
    public void saveProfileImage(ProfileImage profileImage, Employee employee) {
        LocalDateTime now = LocalDateTime.now();
        profileImage.setCreatedAt(now);
        profileImage.setUpdatedAt(now);
        profileImageRepository.save(profileImage);
    }

    // プロフィール画像のアップデート
    @Transactional
    public ErrorKinds updateProfileImage(ProfileImage profileImage) {
        // ファイルサイズチェック
        if (profileImage.getFileSize() > MAX_FILE_SIZE) {
            return ErrorKinds.FILESIZE_ERROR;
        }

        // ファイルタイプチェック
        String contentType = profileImage.getFileType();
        if (!profileImage.getFilePath().equals(NOIMAGE_FILE_PATH)) {
            if (!contentType.startsWith("image/")) {
                return ErrorKinds.FILETYPE_ERROR;
            }
        }
        return ErrorKinds.SUCCESS;
    }
    // プロフィール画像のアップデート （先に従業員を保存するために切り分け)
    public void profileImageUpdate(ProfileImage profileImage) {
        LocalDateTime now = LocalDateTime.now();
        profileImage.setCreatedAt(profileImage.getCreatedAt());
        profileImage.setUpdatedAt(now);
        profileImageRepository.save(profileImage);
    }

    // 1件を検索
    public ProfileImage findByCode(Employee employee) {
        Optional<ProfileImage> option = profileImageRepository.findByEmployee(employee);
        ProfileImage profileImage = option.orElse(null);
        return profileImage;
    }

    // プロフィール画像のパスを取得
    public String getProfileImagePath(Employee employee) {
        ProfileImage profileImage = findByCode(employee);
        if (profileImage != null) {
            String filePath = profileImage.getFilePath();
            if (filePath.equals(NOIMAGE_FILE_PATH)) {
                return NOIMAGE_FILE_PATH;
            }
        } else {
            return NOIMAGE_FILE_PATH;
        }
        return LOCAL_HOST + profileImage.getFilePath();
    }
    // ファイルの拡張子を返す
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    // 従業員リストに基づいて、各従業員のプロフィール画像のファイルパスを取得し、従業員コードと対応付けたマップを作成
    public Map<Integer, String> getProfileImageMap(List<Employee> employeeList) {
        Map<Integer, String> profileImageMap = new HashMap<>();
        for (Employee employee : employeeList) {
            ProfileImage profileImage = findByCode(employee);
            if (profileImage != null) {
                String filePath = LOCAL_HOST + profileImage.getFilePath();
                // 画像パスが既定の「noimage」の場合はそのまま使う
                if (profileImage.getFilePath().equals(NOIMAGE_FILE_PATH)) {
                    filePath = profileImage.getFilePath();
                }
                profileImageMap.put(employee.getCode(), filePath);
            }
        }
        return profileImageMap;
    }

    // ディレクトリ存在チェック
    public boolean isDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        return Files.exists(path) && Files.isDirectory(path);
    }

    // ファイル存在チェック
    public boolean isFileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
}
