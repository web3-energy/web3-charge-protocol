package w3cp.cp.logic.status.feeders.chargeport.one.connector;

import w3cp.model.status.charging.Connector;

public record EnergyConfigEvent(Connector.EnergyDirection direction, Connector.CurrentType currentType) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setEnergyDirection(direction);
    state.setCurrentType(currentType);
  }
}
