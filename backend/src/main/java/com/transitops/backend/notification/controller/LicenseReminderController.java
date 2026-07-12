package com.transitops.backend.notification.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.notification.dto.LicenseExpiryAlert;
import com.transitops.backend.notification.service.LicenseReminderService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/license-reminders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
public class LicenseReminderController {
    private final LicenseReminderService service;

    @GetMapping("/expiring")
    public List<LicenseExpiryAlert> expiring(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        return service.expiringWithin(days);
    }
}
