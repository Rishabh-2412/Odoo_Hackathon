package com.transitops.backend.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transitops.backend.document.model.VehicleDocument;
import com.transitops.backend.document.model.VehicleDocumentType;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {
    boolean existsByVehicleId(Long vehicleId);
    List<VehicleDocument> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<VehicleDocument> findByVehicleIdAndDocumentTypeOrderByCreatedAtDesc(Long vehicleId, VehicleDocumentType documentType);
}
