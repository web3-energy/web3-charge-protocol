package w3cp.model.status.charging;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class PortThermalInfo {
  @Nullable private TemperatureValue connector;  // Connector temperature
  @Nullable private TemperatureValue cable;      // Cable temperature
  @Nullable private TemperatureValue inlet;      // Inlet temperature
  @Nullable private TemperatureValue socket;     // Socket temperature

  @Getter
  @Setter
  public static class TemperatureValue {
    @Nullable private Double value;           // Temperature reading; null = sensor not present/failed
    @Nullable private TemperatureUnit unit;   // e.g., celsius; null = not reported
  }

  public enum TemperatureUnit {
    celsius,
    fahrenheit,
    unknown
  }
}