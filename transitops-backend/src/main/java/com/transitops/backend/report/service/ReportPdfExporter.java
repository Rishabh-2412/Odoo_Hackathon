package com.transitops.backend.report.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import com.transitops.backend.report.dto.FleetAnalyticsResponse;
import com.transitops.backend.report.dto.VehicleAnalyticsResponse;

@Component
public class ReportPdfExporter {
    public byte[] export(FleetAnalyticsResponse report) {
        try (PdfWriter writer = new PdfWriter()) {
            writer.heading("TransitOps Fleet Analytics Report");
            writer.line("Generated: " + report.generatedAt());
            writer.line("Fleet utilization: " + decimal(report.fleetUtilizationPercent()) + "%");
            writer.line("Total distance: " + decimal(report.totalDistanceKm()) + " km");
            writer.line("Total fuel: " + decimal(report.totalFuelLiters()) + " L");
            writer.line("Average efficiency: " + decimal(report.averageFuelEfficiencyKmPerLiter()) + " km/L");
            writer.line("Operational cost: " + decimal(report.totalOperationalCost()));
            writer.line("Revenue: " + decimal(report.totalRevenue()));
            writer.blank();
            writer.subheading("Vehicle performance");
            for (VehicleAnalyticsResponse vehicle : report.vehicles()) {
                writer.line(vehicle.registrationNumber() + " | " + vehicle.nameModel()
                        + " | Distance " + decimal(vehicle.completedDistanceKm()) + " km"
                        + " | Efficiency " + decimal(vehicle.fuelEfficiencyKmPerLiter()) + " km/L"
                        + " | Cost " + decimal(vehicle.operationalCost())
                        + " | ROI " + decimal(vehicle.roiPercent()) + "%");
            }
            return writer.bytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate PDF report", ex);
        }
    }

    private String decimal(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static final class PdfWriter implements AutoCloseable {
        private final PDDocument document = new PDDocument();
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        private PdfWriter() throws IOException {
            newPage();
        }

        private void heading(String text) throws IOException {
            write(text, bold, 18, 24);
        }

        private void subheading(String text) throws IOException {
            write(text, bold, 13, 18);
        }

        private void line(String text) throws IOException {
            write(trimToPdfSafe(text), regular, 9, 14);
        }

        private void blank() {
            y -= 8;
        }

        private void write(String text, PDType1Font font, float size, float spacing) throws IOException {
            if (y < 55) {
                newPage();
            }
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(45, y);
            stream.showText(text);
            stream.endText();
            y -= spacing;
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = 800;
        }

        private byte[] bytes() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
            document.save(output);
            return output.toByteArray();
        }

        private String trimToPdfSafe(String value) {
            String ascii = value.replaceAll("[^\\x20-\\x7E]", "?");
            return ascii.length() <= 120 ? ascii : ascii.substring(0, 117) + "...";
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
            document.close();
            output.close();
        }
    }
}
