package w3cp.cp.logic.status.feeders.chargeport.one.connector;

import w3cp.model.status.charging.Connector;

public record ControlPilotSampleEvent(double cpVoltage, double pwmDutyCycle) implements ConnectorEvent {
  @Override
  public void applyTo(ConnectorRuntimeState state) {
    state.setLastRawCpVoltage(cpVoltage);
    state.setLastRawPwmDutyCycle(pwmDutyCycle);
    state.setPwmDutyCycle(pwmDutyCycle);
    state.setIec61851State(inferIec61851State(cpVoltage));
  }

  private Connector.Iec61851State inferIec61851State(double cpVoltage) {
    if (cpVoltage > 10.0) return Connector.Iec61851State.a;
    if (cpVoltage > 7.5) return Connector.Iec61851State.b;
    if (cpVoltage > 4.5) return Connector.Iec61851State.c;
    if (cpVoltage > 1.5) return Connector.Iec61851State.d;
    if (cpVoltage > -1.0) return Connector.Iec61851State.e;
    return Connector.Iec61851State.f;
  }
}
