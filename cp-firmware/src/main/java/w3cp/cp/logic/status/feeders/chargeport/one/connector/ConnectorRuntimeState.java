package w3cp.cp.logic.status.feeders.chargeport.one.connector;

import lombok.Getter;
import lombok.Setter;
import w3cp.model.status.charging.Connector;

@Getter
@Setter
public class ConnectorRuntimeState {
  private Connector.ConnectorStatus status;
  private Boolean locked;
  private Connector.Iec61851State iec61851State;
  private Double pwmDutyCycle;
  private Boolean relayClosed;
  private Connector.Iec61851Edition iec61851Edition;
  private Connector.Iec61851ChargingMode chargingMode;
  private Connector.EnergyDirection energyDirection;
  private Connector.InterfaceType interfaceType;
  private Connector.CurrentType currentType;
  private String connectorStandard;
  
  private Double lastRawCpVoltage;
  private Double lastRawPwmDutyCycle;

  public ConnectorRuntimeState() {
    this.iec61851State = Connector.Iec61851State.a;
    this.pwmDutyCycle = 0.0;
    this.relayClosed = false;
    this.locked = false;
    this.chargingMode = Connector.Iec61851ChargingMode.mode3;
    this.interfaceType = Connector.InterfaceType.plug;
    this.currentType = Connector.CurrentType.ac;
    this.connectorStandard = "type2";
    this.status = Connector.ConnectorStatus.available;
  }

  public Connector toSnapshotDto() {
    Connector connector = new Connector();
    connector.setStatus(status);
    connector.setLocked(locked);
    connector.setIec61851State(iec61851State);
    connector.setPwmDutyCycle(pwmDutyCycle);
    connector.setRelayClosed(relayClosed);
    connector.setIec61851Edition(iec61851Edition);
    connector.setChargingMode(chargingMode);
    connector.setEnergyDirection(energyDirection);
    connector.setInterfaceType(interfaceType);
    connector.setCurrentType(currentType);
    connector.setConnectorStandard(connectorStandard);
    return connector;
  }
}
