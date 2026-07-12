package com.transitops.backend.report.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.report.dto.FleetAnalyticsResponse;
import com.transitops.backend.report.dto.VehicleAnalyticsResponse;
import com.transitops.backend.report.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','FINANCIAL_ANALYST')")
public class ReportController {
    private final ReportService service;

    @GetMapping("/fleet")
    public FleetAnalyticsResponse fleet() {
        return service.fleetAnalytics();
    }

    @GetMapping("/vehicles/{vehicleId}")
    public VehicleAnalyticsResponse vehicle(@PathVariable Long vehicleId) {
        return service.vehicleAnalytics(vehicleId);
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> csv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("transitops-fleet-report.csv").build().toString())
                .body(service.exportCsv());
    }

    @GetMapping(value = "/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("transitops-fleet-report.pdf").build().toString())
                .body(service.exportPdf());
    }
}
