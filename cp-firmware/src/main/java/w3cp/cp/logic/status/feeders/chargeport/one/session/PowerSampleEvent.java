package w3cp.cp.logic.status.feeders.chargeport.one.session;

import java.time.Instant;

public record PowerSampleEvent(
    Instant at,
    double importW,
    double exportW,
    double meterImportKWh,
    double meterExportKWh
) implements SessionEvent {
}
