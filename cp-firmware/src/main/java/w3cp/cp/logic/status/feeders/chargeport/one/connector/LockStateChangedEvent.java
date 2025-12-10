package w3cp.cp.logic.status.feeders.chargeport.one.connector;

public record LockStateChangedEvent(boolean locked) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setLocked(locked);
  }
}
