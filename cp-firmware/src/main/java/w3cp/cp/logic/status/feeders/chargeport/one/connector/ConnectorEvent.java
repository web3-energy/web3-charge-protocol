package w3cp.cp.logic.status.feeders.chargeport.one.connector;

public sealed interface ConnectorEvent permits 
    ControlPilotSampleEvent,
    LockStateChangedEvent,
    RelayStateChangedEvent,
    ModeDetectedEvent,
    EnergyConfigEvent,
    PhysicalConfigEvent {
  
  void applyTo(ConnectorRuntimeState state);
}
