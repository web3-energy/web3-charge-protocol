package w3cp.cp.logic.status.feeders.chargeport.one.connector;

import w3cp.model.status.charging.Connector;

public record ModeDetectedEvent(Connector.Iec61851ChargingMode mode, Connector.Iec61851Edition edition) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setChargingMode(mode);
    state.setIec61851Edition(edition);
  }
}
