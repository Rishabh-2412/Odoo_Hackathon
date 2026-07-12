package com.transitops.backend.trip.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.transitops.backend.common.exception.BusinessRuleException;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.model.DriverStatus;
import com.transitops.backend.driver.repository.DriverRepository;
import com.transitops.backend.fuel.repository.FuelLogRepository;
import com.transitops.backend.trip.dto.CreateTripRequest;
import com.transitops.backend.trip.model.Trip;
import com.transitops.backend.trip.model.TripStatus;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.model.VehicleType;
import com.transitops.backend.vehicle.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {
    @Mock private TripRepository tripRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private FuelLogRepository fuelLogRepository;

    private TripService service;
    private Vehicle vehicle;
    private Driver driver;

    @BeforeEach
    void setUp() {
        service = new TripService(tripRepository, vehicleRepository, driverRepository, fuelLogRepository);
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setRegistrationNumber("TN01AB1234");
        vehicle.setNameModel("Van-05");
        vehicle.setType(VehicleType.VAN);
        vehicle.setRegion("South");
        vehicle.setMaxLoadCapacityKg(new BigDecimal("500"));
        vehicle.setOdometerKm(new BigDecimal("1000"));
        vehicle.setAcquisitionCost(new BigDecimal("1000000"));
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        driver = new Driver();
        driver.setId(1L);
        driver.setName("Alex");
        driver.setLicenseNumber("DL123");
        driver.setLicenseCategory("LMV");
        driver.setLicenseExpiryDate(LocalDate.now().plusYears(1));
        driver.setContactNumber("9999999999");
        driver.setRegion("South");
        driver.setSafetyScore(new BigDecimal("95"));
        driver.setStatus(DriverStatus.AVAILABLE);
    }

    @Test
    void rejectsCargoAboveVehicleCapacity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        CreateTripRequest request = new CreateTripRequest(
                "A", "B", 1L, 1L, new BigDecimal("600"), new BigDecimal("100"), null);

        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.createDraft(request));
        assertEquals("CARGO_CAPACITY_EXCEEDED", error.getCode());
    }

    @Test
    void dispatchChangesVehicleAndDriverStatuses() {
        Trip trip = new Trip();
        trip.setId(10L);
        trip.setSource("A");
        trip.setDestination("B");
        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setCargoWeightKg(new BigDecimal("450"));
        trip.setPlannedDistanceKm(new BigDecimal("100"));
        trip.setStartOdometerKm(vehicle.getOdometerKm());
        trip.setStatus(TripStatus.DRAFT);

        when(tripRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(trip));
        when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(driver));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.dispatch(10L);

        assertEquals(TripStatus.DISPATCHED, trip.getStatus());
        assertEquals(VehicleStatus.ON_TRIP, vehicle.getStatus());
        assertEquals(DriverStatus.ON_TRIP, driver.getStatus());
    }
}
