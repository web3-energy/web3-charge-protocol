package w3cp.model.status.charging;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class ChargePort {
  @Positive
  private int chargePortId;                    // Stable positive integer index (1, 2, 3, ...)
  @Nullable private Connector connector;
  @Nullable private EnergyStatus metering;     // CP meters + instantaneous values
  @Nullable private ChargeSession session;     // current/last physical charging session
  @Nullable private PortThermalInfo thermalInfo;   // port-specific thermal telemetry
  @Nullable private EvInfo evInfo;             // what is connected to the CP to charge

}