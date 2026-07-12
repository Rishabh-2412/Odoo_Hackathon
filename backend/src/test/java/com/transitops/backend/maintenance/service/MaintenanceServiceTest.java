package com.transitops.backend.maintenance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.transitops.backend.maintenance.dto.CreateMaintenanceRequest;
import com.transitops.backend.maintenance.model.MaintenanceLog;
import com.transitops.backend.maintenance.model.MaintenanceStatus;
import com.transitops.backend.maintenance.repository.MaintenanceLogRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {
    @Mock private MaintenanceLogRepository maintenanceRepository;
    @Mock private VehicleRepository vehicleRepository;

    @Test
    void creatingMaintenanceMovesVehicleToShop() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setRegistrationNumber("TN01AB1234");
        vehicle.setNameModel("Van-05");
        vehicle.setOdometerKm(new BigDecimal("1000"));
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));
        when(maintenanceRepository.existsByVehicleIdAndStatus(1L, MaintenanceStatus.ACTIVE)).thenReturn(false);
        when(maintenanceRepository.save(any(MaintenanceLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceService service = new MaintenanceService(maintenanceRepository, vehicleRepository);
        service.create(new CreateMaintenanceRequest(
                1L, "Oil Change", "Scheduled service", LocalDate.now(),
                new BigDecimal("2500"), null));

        assertEquals(VehicleStatus.IN_SHOP, vehicle.getStatus());
    }
}
