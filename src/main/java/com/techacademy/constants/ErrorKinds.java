package com.techacademy.constants;

// エラーメッセージ定義
public enum ErrorKinds {

    // エラー内容
    // 空白チェックエラー
    BLANK_ERROR,
    // メール空白チェックエラー
    EMAIL_BLANK_ERROR,
    // 半角英数字チェックエラー
    HALFSIZE_ERROR,
    // 桁数(8桁~16桁以外)チェックエラー
    RANGECHECK_ERROR,
    // 重複チェックエラー(例外あり)
    DUPLICATE_EXCEPTION_ERROR,
    // 重複チェックエラー(例外なし)
    DUPLICATE_ERROR,
    // ログイン中削除チェックエラー
    LOGINCHECK_ERROR,
    // 日付チェックエラー
    DATECHECK_ERROR,
    // プロフィールファイルサイズエラー
    FILESIZE_ERROR,
    // プロフィールファイルタイプエラー
    FILETYPE_ERROR,
    // プロフィールファイルサイズエラー
    REPORT_FILESIZE_ERROR,
    // プロフィールファイルタイプエラー
    REPORT_FILETYPE_ERROR,
    // チェックOK
    CHECK_OK,
    // 正常終了
    SUCCESS;

}
