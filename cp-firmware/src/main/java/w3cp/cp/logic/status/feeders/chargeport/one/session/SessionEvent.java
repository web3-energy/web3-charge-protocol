package w3cp.cp.logic.status.feeders.chargeport.one.session;

public sealed interface SessionEvent permits
    VehiclePluggedEvent,
    VehicleUnpluggedEvent,
    PowerSampleEvent {
}
