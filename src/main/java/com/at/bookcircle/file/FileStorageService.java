package com.at.bookcircle.file;

import com.at.bookcircle.book.Book;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;
    public String saveFile(@NonNull MultipartFile sourceFile, @NonNull Integer userId) {
        String fileUploadSubPath = "users" + File.separator + userId;

        return uploadFile(sourceFile,fileUploadSubPath);
    }

    private String uploadFile(@NonNull MultipartFile sourceFile, @NonNull String fileUploadSubPath) {
            final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
            try {
                File targetFolder = new File(finalUploadPath);
                if (!targetFolder.exists()) {
                    boolean folderCreated = targetFolder.mkdirs();
                    if(!folderCreated) {
                        log.warn("Failed to create target folder");
                        return null;
                    }
                }
            }catch (Exception e) {
                log.warn("Failed to make target folder: {}",e.getMessage());
            }
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
            String targetFilePath = finalUploadPath +File.separator+ System.currentTimeMillis() + "." + fileExtension;
            Path targetPath = Paths.get(targetFilePath);
            try {
                Files.write(targetPath, sourceFile.getBytes());
                log.info("Successfully saved file to: {}",targetFilePath);
                return targetFilePath;
            }catch (IOException e) {
                log.error("Failed to save file: {}",e.getMessage());
            }
            return null;
    }

    private String getFileExtension(String fileName) {

        if(fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if(lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
