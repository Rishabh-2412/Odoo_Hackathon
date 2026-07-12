package com.transitops.backend.document.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.transitops.backend.common.exception.FileStorageException;
import com.transitops.backend.common.exception.ResourceNotFoundException;
import com.transitops.backend.config.AppProperties;
import com.transitops.backend.document.dto.VehicleDocumentResponse;
import com.transitops.backend.document.model.VehicleDocument;
import com.transitops.backend.document.model.VehicleDocumentType;
import com.transitops.backend.document.repository.VehicleDocumentRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleDocumentService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/webp");

    private final VehicleDocumentRepository repository;
    private final VehicleRepository vehicleRepository;
    private final AppProperties properties;

    @Transactional
    public VehicleDocumentResponse upload(
            Long vehicleId,
            VehicleDocumentType type,
            LocalDate expiryDate,
            MultipartFile file) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        validate(file);
        Path root = storageRoot();
        try {
            Files.createDirectories(root);
            String extension = safeExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + extension;
            Path target = root.resolve(storedName).normalize();
            if (!target.startsWith(root)) {
                throw new FileStorageException("Invalid storage path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            VehicleDocument document = new VehicleDocument();
            document.setVehicle(vehicle);
            document.setDocumentType(type);
            document.setOriginalFileName(safeOriginalName(file.getOriginalFilename()));
            document.setStoredFileName(storedName);
            document.setContentType(file.getContentType());
            document.setSizeBytes(file.getSize());
            document.setExpiryDate(expiryDate);
            return toResponse(repository.save(document));
        } catch (IOException ex) {
            throw new FileStorageException("Unable to store the uploaded file", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleDocumentResponse> list(Long vehicleId, VehicleDocumentType type) {
        List<VehicleDocument> documents = type == null
                ? repository.findByVehicleIdOrderByCreatedAtDesc(vehicleId)
                : repository.findByVehicleIdAndDocumentTypeOrderByCreatedAtDesc(vehicleId, type);
        return documents.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DownloadedDocument download(Long id) {
        VehicleDocument document = requireDocument(id);
        try {
            Path path = storageRoot().resolve(document.getStoredFileName()).normalize();
            if (!path.startsWith(storageRoot()) || !Files.exists(path)) {
                throw new ResourceNotFoundException("Stored document file was not found");
            }
            Resource resource = new UrlResource(path.toUri());
            return new DownloadedDocument(document, resource);
        } catch (IOException ex) {
            throw new FileStorageException("Unable to read the stored document", ex);
        }
    }

    @Transactional
    public void delete(Long id) {
        VehicleDocument document = requireDocument(id);
        Path path = storageRoot().resolve(document.getStoredFileName()).normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("Unable to delete the stored document", ex);
        }
        repository.delete(document);
    }

    private VehicleDocument requireDocument(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle document not found: " + id));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("A non-empty file is required");
        }
        if (file.getSize() > properties.getStorage().getMaxFileSizeBytes()) {
            throw new FileStorageException("File size exceeds the configured limit");
        }
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new FileStorageException("Only PDF, JPEG, PNG, and WebP files are supported");
        }
    }

    private Path storageRoot() {
        return properties.getStorage().getUploadDir().toAbsolutePath().normalize();
    }

    private String safeOriginalName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        String normalized = fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String baseName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return baseName.replaceAll("[\\r\\n]", "_");
    }

    private String safeExtension(String fileName) {
        String safe = safeOriginalName(fileName);
        int dot = safe.lastIndexOf('.');
        if (dot < 0 || dot == safe.length() - 1) {
            return "";
        }
        String extension = safe.substring(dot).toLowerCase();
        return extension.matches("\\.(pdf|jpg|jpeg|png|webp)") ? extension : "";
    }

    public VehicleDocumentResponse toResponse(VehicleDocument document) {
        return new VehicleDocumentResponse(
                document.getId(), document.getVehicle().getId(), document.getVehicle().getRegistrationNumber(),
                document.getDocumentType(), document.getOriginalFileName(), document.getContentType(),
                document.getSizeBytes(), document.getExpiryDate(),
                document.getExpiryDate() != null && document.getExpiryDate().isBefore(LocalDate.now()),
                document.getCreatedAt());
    }

    public record DownloadedDocument(VehicleDocument metadata, Resource resource) {
    }
}
