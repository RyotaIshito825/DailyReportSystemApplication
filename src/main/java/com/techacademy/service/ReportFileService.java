package com.techacademy.service;

import java.io.File;
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
import com.techacademy.entity.Report;
import com.techacademy.entity.ReportFile;
import com.techacademy.repository.ReportFileRepository;

@Service
public class ReportFileService {

    private final ReportFileRepository reportFileRepository;
    // アップロード先のディレクトリ
    private static final String UPLOAD_DIR = "/Users/ryotaishito/DailyReportSystemApplicationImages";
    private static final String LOCAL_HOST = "http://localhost:8080";
    private static final String REPORTFILES_DIR = "reportFiles";
    private static final String NOIMAGE_FILE_PATH = "../../img/report-nofile.png";

    public ReportFileService(ReportFileRepository reportFileRepository) {
        this.reportFileRepository = reportFileRepository;
    }

    // ファイルの最大サイズ(1MB)
    private static final long MAX_FILE_SIZE = 1024 * 1024;

    // レポートファイルの保存
    @Transactional
    public ErrorKinds save(ReportFile reportFile, Report report) {
        // ファイルサイズチェック
        if (reportFile.getFileSize() > MAX_FILE_SIZE) {
            return ErrorKinds.REPORT_FILESIZE_ERROR;
        }
        // ファイルタイプチェック
        String contentType = reportFile.getFileType();
        if (!reportFile.getFilePath().equals(NOIMAGE_FILE_PATH)) {
            if (!contentType.startsWith("image/") || contentType.startsWith("application/pdf")) {
                return ErrorKinds.REPORT_FILETYPE_ERROR;
            }
        }
        return ErrorKinds.SUCCESS;
    }
    // レポートファイルの保存（先にレポートを保存するために切り分け）
    public void saveReportFile(ReportFile reportFile, Report Report) {
        LocalDateTime now = LocalDateTime.now();
        reportFile.setCreatedAt(now);
        reportFile.setUpdatedAt(now);
        reportFileRepository.save(reportFile);
    }

    // レポートファイルのアップデート
    @Transactional
    public ErrorKinds updateReportFile(ReportFile reportFile) {
     // ファイルサイズチェック
        if (reportFile.getFileSize() > MAX_FILE_SIZE) {
            return ErrorKinds.REPORT_FILESIZE_ERROR;
        }
        // ファイルタイプチェック
        String contentType = reportFile.getFileType();
        if (!reportFile.getFilePath().equals(NOIMAGE_FILE_PATH)) {
            if (!contentType.startsWith("image/") || contentType.startsWith("application/pdf")) {
                return ErrorKinds.REPORT_FILETYPE_ERROR;
            }
        }
        return ErrorKinds.SUCCESS;
    }
    // レポートファイルのアップデート （先に従業員を保存するために切り分け)
    public void reportFileUpdate(ReportFile reportFile) {
        LocalDateTime now = LocalDateTime.now();
        reportFile.setCreatedAt(reportFile.getCreatedAt());
        reportFile.setUpdatedAt(now);
        reportFileRepository.save(reportFile);
    }

    // 1件を検索
    public ReportFile findByReport(Report report) {
        Optional<ReportFile> option = reportFileRepository.findByReport(report);
        ReportFile reportFile = option.orElse(null);
        return reportFile;
    }

    // ファイルのパスを取得
    public String getReportFilePath(Report report) {
        ReportFile reportFile = findByReport(report);
        if (reportFile != null) {
            String filePath = reportFile.getFilePath();
            if (filePath.equals(NOIMAGE_FILE_PATH)) {
                return NOIMAGE_FILE_PATH;
            }
        } else {
            return NOIMAGE_FILE_PATH;
        }
        return LOCAL_HOST + reportFile.getFilePath();
    }

    // ファイルの拡張子を返す
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    // レポートリストに基づいて、各レポートのファイルのパスを取得し、レポートIDと対応付けたマップを作成
    public Map<Integer, String> getReportFileMap(List<Report> reportList) {
        Map<Integer, String> reportFileMap = new HashMap<>();
        for (Report report : reportList) {
            ReportFile reportFile = findByReport(report);
            if (reportFile != null) {
                String filePath = LOCAL_HOST + reportFile.getFilePath();
                // 画像パスが規定の「nofile」の場合はそのまま使う
                if (reportFile.getFilePath().equals(NOIMAGE_FILE_PATH)) {
                    filePath = reportFile.getFilePath();
                // ファイルが存在しない場合は、共通ファイルのパスを保存
                } else if (!isFileExists(UPLOAD_DIR + File.separator + REPORTFILES_DIR + File.separator + reportFile.getName() + getFileExtension(reportFile.getFilePath()))) {
                    filePath = NOIMAGE_FILE_PATH;
                }
               reportFileMap.put(report.getId(), filePath);
            }
        }
        return reportFileMap;
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
