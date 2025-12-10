package w3cp.cp.logic.status.feeders.chargeport.one.connector;

import w3cp.model.status.charging.Connector;

public record PhysicalConfigEvent(Connector.InterfaceType interfaceType, String connectorStandard) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setInterfaceType(interfaceType);
    state.setConnectorStandard(connectorStandard);
  }
}
