package com.lcaohoanq.nocket.domain.asset;

import com.lcaohoanq.nocket.component.LocalizationUtils;
import com.lcaohoanq.nocket.constant.BusinessNumber;
import com.lcaohoanq.nocket.constant.MessageKey;
import com.lcaohoanq.nocket.exception.FileTooLargeException;
import com.lcaohoanq.nocket.exception.UnsupportedMediaTypeException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStoreService implements IFileStoreService {

    private final LocalizationUtils localizationUtils;

    public FileStoreService(LocalizationUtils localizationUtils) {
        this.localizationUtils = localizationUtils;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID() + "_" + filename;
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        //File.separator: depends on the OS, for windows it is '\', for linux it is '/'
        Path destination = Paths.get(uploadDir + File.separator + uniqueFileName);
        //copy the file to the destination
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }

    @Override
    public MultipartFile validateProductImage(MultipartFile file) throws IOException {
        // Kiểm tra kích thước file và định dạng
        if (file.getSize() == 0) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
            throw new FileTooLargeException("File is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new UnsupportedMediaTypeException("Unsupported media type");
        }
        return file;
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

}
