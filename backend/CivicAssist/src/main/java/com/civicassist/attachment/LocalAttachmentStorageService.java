package com.civicassist.attachment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalAttachmentStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    @Value("${civicassist.attachments.storage-dir:./data/complaint-attachments}")
    private String storageDirectory;

    public record StoredFile(
            String filename,
            String contentType,
            long size
    ) {}

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB"
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "Original filename is required"
            );
        }

        String contentType = detectContentType(file);

        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> throw new IllegalArgumentException(
                    "Only JPEG, PNG, and PDF files are allowed"
            );
        };

        Path directory = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(directory);

        String storedFilename = UUID.randomUUID() + extension;
        Path target = directory.resolve(storedFilename).normalize();

        if (!target.getParent().equals(directory)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        } catch (IOException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }

        return new StoredFile(
                storedFilename,
                contentType,
                Files.size(target)
        );
    }

    public Path resolve(String storedFilename) {
        Path directory = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();

        Path target = directory.resolve(storedFilename).normalize();

        if (!target.getParent().equals(directory)) {
            throw new IllegalArgumentException("Invalid stored filename");
        }

        return target;
    }

    public void delete(String storedFilename) throws IOException {
        Files.deleteIfExists(resolve(storedFilename));
    }

    private String detectContentType(MultipartFile file)
            throws IOException {

        byte[] header = new byte[8];

        int bytesRead;
        try (InputStream input = file.getInputStream()) {
            bytesRead = input.read(header);
        }

        if (bytesRead >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        if (bytesRead >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A) {
            return "image/png";
        }

        if (bytesRead >= 5
                && header[0] == '%'
                && header[1] == 'P'
                && header[2] == 'D'
                && header[3] == 'F'
                && header[4] == '-') {
            return "application/pdf";
        }

        throw new IllegalArgumentException(
                "File content does not match a supported file type"
        );
    }


}