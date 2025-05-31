package com.incubadora.SGEI.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_REPORT_EXTENSIONS = new HashSet<>(Set.of(".pdf", ".doc", ".docx"));
    private static final Set<String> ALLOWED_PITCH_EXTENSIONS = new HashSet<>(Set.of(".pdf", ".ppt", ".pptx"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService() {
        try {
            Files.createDirectories(Path.of(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload", e);
        }
    }

    public String salvarRelatorio(MultipartFile file) {
        validateFile(file, ALLOWED_REPORT_EXTENSIONS);
        return storeFile(file, "relatorio");
    }

    public String salvarPitch(MultipartFile file) {
        validateFile(file, ALLOWED_PITCH_EXTENSIONS);
        return storeFile(file, "pitch");
    }

    public Resource carregarArquivo(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Arquivo não encontrado: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar arquivo: " + fileName, e);
        }
    }

    private String storeFile(MultipartFile file, String prefix) {
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = prefix + "_" + UUID.randomUUID().toString() + extension;
            Path targetLocation = getFilePath(newFileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + file.getOriginalFilename(), e);
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedExtensions) {
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new RuntimeException("Nome de arquivo inválido");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("Tipo de arquivo não permitido");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo permitido: 10MB");
        }
    }

    public Path getFilePath(String fileName) {
        return Path.of(uploadDir).resolve(fileName);
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar arquivo: " + fileName, e);
        }
    }
} 