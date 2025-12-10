package w3cp.cp.logic.status.feeders.chargeport.one.connector;

public record RelayStateChangedEvent(boolean relayClosed) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setRelayClosed(relayClosed);
  }
}
