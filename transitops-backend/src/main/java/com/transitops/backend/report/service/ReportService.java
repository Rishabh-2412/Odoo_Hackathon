package com.transitops.backend.report.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.expense.model.Expense;
import com.transitops.backend.expense.repository.ExpenseRepository;
import com.transitops.backend.fuel.model.FuelLog;
import com.transitops.backend.fuel.repository.FuelLogRepository;
import com.transitops.backend.maintenance.model.MaintenanceLog;
import com.transitops.backend.maintenance.repository.MaintenanceLogRepository;
import com.transitops.backend.report.dto.FleetAnalyticsResponse;
import com.transitops.backend.report.dto.MonthlyCostPoint;
import com.transitops.backend.report.dto.VehicleAnalyticsResponse;
import com.transitops.backend.trip.repository.TripRepository;
import com.transitops.backend.vehicle.model.Vehicle;
import com.transitops.backend.vehicle.model.VehicleStatus;
import com.transitops.backend.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceLogRepository maintenanceRepository;
    private final ExpenseRepository expenseRepository;
    private final ReportPdfExporter pdfExporter;

    @Transactional(readOnly = true)
    public FleetAnalyticsResponse fleetAnalytics() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<VehicleAnalyticsResponse> rows = vehicles.stream().map(this::vehicleAnalytics).toList();
        BigDecimal totalDistance = sum(rows, VehicleAnalyticsResponse::completedDistanceKm);
        BigDecimal totalFuel = sum(rows, VehicleAnalyticsResponse::fuelLiters);
        BigDecimal totalFuelCost = sum(rows, VehicleAnalyticsResponse::fuelCost);
        BigDecimal totalMaintenance = sum(rows, VehicleAnalyticsResponse::maintenanceCost);
        BigDecimal totalOther = sum(rows, VehicleAnalyticsResponse::otherExpenseCost);
        BigDecimal totalOperational = sum(rows, VehicleAnalyticsResponse::operationalCost);
        BigDecimal totalRevenue = sum(rows, VehicleAnalyticsResponse::revenue);
        BigDecimal efficiency = totalFuel.signum() == 0 ? BigDecimal.ZERO
                : totalDistance.divide(totalFuel, 2, RoundingMode.HALF_UP);
        long activeFleet = vehicles.stream().filter(v -> v.getStatus() != VehicleStatus.RETIRED).count();
        long onTrip = vehicles.stream().filter(v -> v.getStatus() == VehicleStatus.ON_TRIP).count();
        BigDecimal utilization = activeFleet == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(onTrip).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(activeFleet), 2, RoundingMode.HALF_UP);

        return new FleetAnalyticsResponse(
                Instant.now(), utilization, totalDistance, totalFuel, efficiency,
                totalFuelCost, totalMaintenance, totalOther, totalOperational, totalRevenue,
                rows, monthlyTrend(6));
    }

    @Transactional(readOnly = true)
    public VehicleAnalyticsResponse vehicleAnalytics(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new com.transitops.backend.common.exception.ResourceNotFoundException(
                        "Vehicle not found: " + vehicleId));
        return vehicleAnalytics(vehicle);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        FleetAnalyticsResponse report = fleetAnalytics();
        StringBuilder csv = new StringBuilder();
        csv.append("Vehicle ID,Registration Number,Name/Model,Distance Km,Fuel Liters,Fuel Efficiency Km/L,Fuel Cost,Maintenance Cost,Other Expenses,Operational Cost,Revenue,Acquisition Cost,ROI %\n");
        for (VehicleAnalyticsResponse row : report.vehicles()) {
            csv.append(row.vehicleId()).append(',')
                    .append(csv(row.registrationNumber())).append(',')
                    .append(csv(row.nameModel())).append(',')
                    .append(row.completedDistanceKm()).append(',')
                    .append(row.fuelLiters()).append(',')
                    .append(row.fuelEfficiencyKmPerLiter()).append(',')
                    .append(row.fuelCost()).append(',')
                    .append(row.maintenanceCost()).append(',')
                    .append(row.otherExpenseCost()).append(',')
                    .append(row.operationalCost()).append(',')
                    .append(row.revenue()).append(',')
                    .append(row.acquisitionCost()).append(',')
                    .append(row.roiPercent()).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf() {
        return pdfExporter.export(fleetAnalytics());
    }

    private VehicleAnalyticsResponse vehicleAnalytics(Vehicle vehicle) {
        BigDecimal distance = safe(tripRepository.sumCompletedDistanceByVehicle(vehicle.getId()));
        BigDecimal fuelLiters = safe(fuelLogRepository.sumLitersByVehicle(vehicle.getId()));
        BigDecimal fuelCost = safe(fuelLogRepository.sumCostByVehicle(vehicle.getId()));
        BigDecimal maintenanceCost = safe(maintenanceRepository.sumCostByVehicle(vehicle.getId()));
        BigDecimal otherExpenses = safe(expenseRepository.sumAmountByVehicle(vehicle.getId()));
        BigDecimal operationalCost = fuelCost.add(maintenanceCost).add(otherExpenses);
        BigDecimal revenue = safe(tripRepository.sumRevenueByVehicle(vehicle.getId()));
        BigDecimal efficiency = fuelLiters.signum() == 0 ? BigDecimal.ZERO
                : distance.divide(fuelLiters, 2, RoundingMode.HALF_UP);
        BigDecimal roi = vehicle.getAcquisitionCost().signum() == 0 ? BigDecimal.ZERO
                : revenue.subtract(fuelCost.add(maintenanceCost))
                        .divide(vehicle.getAcquisitionCost(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
        return new VehicleAnalyticsResponse(
                vehicle.getId(), vehicle.getRegistrationNumber(), vehicle.getNameModel(), distance,
                fuelLiters, efficiency, fuelCost, maintenanceCost, otherExpenses, operationalCost,
                revenue, vehicle.getAcquisitionCost(), roi);
    }

    private List<MonthlyCostPoint> monthlyTrend(int months) {
        YearMonth current = YearMonth.now();
        Map<YearMonth, CostAccumulator> values = new LinkedHashMap<>();
        for (int i = months - 1; i >= 0; i--) {
            values.put(current.minusMonths(i), new CostAccumulator());
        }
        for (FuelLog log : fuelLogRepository.findAll()) {
            CostAccumulator accumulator = values.get(YearMonth.from(log.getLogDate()));
            if (accumulator != null) accumulator.fuel = accumulator.fuel.add(log.getCost());
        }
        for (MaintenanceLog log : maintenanceRepository.findAll()) {
            LocalDate date = log.getEndDate() == null ? log.getStartDate() : log.getEndDate();
            CostAccumulator accumulator = values.get(YearMonth.from(date));
            if (accumulator != null) accumulator.maintenance = accumulator.maintenance.add(log.getCost());
        }
        for (Expense expense : expenseRepository.findAll()) {
            CostAccumulator accumulator = values.get(YearMonth.from(expense.getExpenseDate()));
            if (accumulator != null) accumulator.other = accumulator.other.add(expense.getAmount());
        }
        List<MonthlyCostPoint> result = new ArrayList<>();
        values.forEach((month, value) -> result.add(new MonthlyCostPoint(
                month, value.fuel, value.maintenance, value.other,
                value.fuel.add(value.maintenance).add(value.other))));
        return result;
    }

    private BigDecimal sum(List<VehicleAnalyticsResponse> rows,
            java.util.function.Function<VehicleAnalyticsResponse, BigDecimal> extractor) {
        return rows.stream().map(extractor).map(this::safe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String csv(String value) {
        String escaped = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static final class CostAccumulator {
        private BigDecimal fuel = BigDecimal.ZERO;
        private BigDecimal maintenance = BigDecimal.ZERO;
        private BigDecimal other = BigDecimal.ZERO;
    }
}
