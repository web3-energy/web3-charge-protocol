package w3cp.cp.logic.status.feeders.chargeport.one.session;

import java.time.Instant;

public record VehiclePluggedEvent(Instant at) implements SessionEvent {
}
