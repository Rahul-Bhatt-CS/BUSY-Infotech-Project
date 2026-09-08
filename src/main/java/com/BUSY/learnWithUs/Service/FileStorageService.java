package com.BUSY.learnWithUs.Service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Path UPLOAD_DIR =
            Paths.get("uploads", "lessons")
                    .toAbsolutePath()
                    .normalize();

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "ppt", "pptx");

    public FileStorageService() {
        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to create lesson upload directory", e
            );
        }
    }

    public String store(MultipartFile file) {
        validate(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename()
        );

        String extension = getExtension(originalName);

        String filename =
                UUID.randomUUID() + "." + extension;

        Path target = UPLOAD_DIR.resolve(filename)
                .normalize();

        if (!target.getParent().equals(UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(
                    input,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to store lesson file", e
            );
        }

        // Store a relative application path in the database.
        return "/uploads/lessons/" + filename;
    }

    public Path load(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new IllegalArgumentException("Lesson file path is missing");
        }

        String filename = storedPath.substring(
                storedPath.lastIndexOf('/') + 1
        );

        if (filename.isBlank()
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid lesson file path");
        }

        Path path = UPLOAD_DIR.resolve(filename)
                .normalize();

        if (!path.getParent().equals(UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid lesson file path");
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Lesson file not found");
        }

        return path;
    }

    public void delete(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return;
        }

        try {
            Path path = load(storedPath);
            Files.deleteIfExists(path);
        } catch (IllegalArgumentException | IOException ignored) {
            // File deletion should not make an otherwise successful
            // lesson operation fail.
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "A PDF, PPT, or PPTX lesson file is required"
            );
        }

        String filename = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename()
        );

        String extension = getExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF, PPT, and PPTX files are allowed"
            );
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');

        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }

        return filename.substring(dot + 1)
                .toLowerCase(Locale.ROOT);
    }
}
