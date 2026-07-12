package com.transitops.backend.trip.controller;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transitops.backend.common.dto.PageResponse;
import com.transitops.backend.trip.dto.CancelTripRequest;
import com.transitops.backend.trip.dto.CompleteTripRequest;
import com.transitops.backend.trip.dto.CreateTripRequest;
import com.transitops.backend.trip.dto.TripResponse;
import com.transitops.backend.trip.dto.UpdateTripRequest;
import com.transitops.backend.trip.model.TripStatus;
import com.transitops.backend.trip.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public TripResponse createDraft(@Valid @RequestBody CreateTripRequest request) {
        return service.createDraft(request);
    }

    @GetMapping
    public PageResponse<TripResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TripStatus status,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return service.list(search, status, vehicleId, driverId, from, to, pageable);
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public TripResponse updateDraft(@PathVariable Long id, @Valid @RequestBody UpdateTripRequest request) {
        return service.updateDraft(id, request);
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public TripResponse dispatch(@PathVariable Long id) {
        return service.dispatch(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public TripResponse complete(@PathVariable Long id, @Valid @RequestBody CompleteTripRequest request) {
        return service.complete(id, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public TripResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelTripRequest request) {
        return service.cancel(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
