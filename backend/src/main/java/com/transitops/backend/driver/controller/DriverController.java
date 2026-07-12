package com.transitops.backend.driver.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.driver.dto.CreateDriverRequest;
import com.transitops.backend.driver.dto.DriverResponse;
import com.transitops.backend.driver.dto.UpdateDriverRequest;
import com.transitops.backend.driver.dto.UpdateDriverStatusRequest;
import com.transitops.backend.driver.model.DriverStatus;
import com.transitops.backend.driver.service.DriverService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
    public DriverResponse create(@Valid @RequestBody CreateDriverRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<DriverResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DriverStatus status,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean licenseValid,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(search, status, region, licenseValid, pageable);
    }

    @GetMapping("/available")
    public List<DriverResponse> available(@RequestParam(required = false) String region) {
        return service.available(region);
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDriverRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SAFETY_OFFICER')")
    public DriverResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateDriverStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
