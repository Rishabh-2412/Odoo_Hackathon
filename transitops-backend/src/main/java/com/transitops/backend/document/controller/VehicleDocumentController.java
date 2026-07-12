package com.transitops.backend.document.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.transitops.backend.document.dto.VehicleDocumentResponse;
import com.transitops.backend.document.model.VehicleDocumentType;
import com.transitops.backend.document.service.VehicleDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicle-documents")
@RequiredArgsConstructor
public class VehicleDocumentController {
    private final VehicleDocumentService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleDocumentResponse upload(
            @RequestParam Long vehicleId,
            @RequestParam VehicleDocumentType documentType,
            @RequestParam(required = false) LocalDate expiryDate,
            @RequestParam MultipartFile file) {
        return service.upload(vehicleId, documentType, expiryDate, file);
    }

    @GetMapping
    public List<VehicleDocumentResponse> list(
            @RequestParam Long vehicleId,
            @RequestParam(required = false) VehicleDocumentType documentType) {
        return service.list(vehicleId, documentType);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        VehicleDocumentService.DownloadedDocument download = service.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.metadata().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.metadata().getOriginalFileName())
                                .build().toString())
                .body(download.resource());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
