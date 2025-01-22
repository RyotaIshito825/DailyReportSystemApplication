'use strict'

{

    // プロフィールイメージのアップロードクリック操作
    let profileImage = document.getElementById("profileImage");
    let profileImageInput = document.getElementById("profileImageInput");
    if (profileImage && profileImageInput) {
        profileImage.addEventListener("click", () => {
            profileImageInput.click();
        });
    }


    // レポートファイルのアップロードクリック操作
    let uploadBtn = document.getElementById("uploadBtn");
    let reportFileInput = document.getElementById("reportFileInput");
    if (uploadBtn && reportFileInput) {
        uploadBtn.addEventListener("click", () => {
            reportFileInput.click();
        });
    }

    // レポートファイルのdeleteクリック操作
    let deleteBtn = document.getElementById("deleteBtn");
    let reportFile = document.getElementById("reportFile");
    if (deleteBtn && reportFile) {
        deleteBtn.addEventListener("click", () => {
            console.log("test");
            reportFile.src = "../../img/report-nofile.png";
        });
    }


}