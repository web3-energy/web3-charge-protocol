package w3cp.model.status;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class SystemThermalInfo {
  @Nullable private TemperatureValue ambient;        // Ambient temperature around the wallbox
  @Nullable private TemperatureValue pcb;            // PCB temperature
  @Nullable private TemperatureValue mcu;            // Microcontroller temperature
  @Nullable private TemperatureValue transformer;    // Transformer temperature
  @Nullable private TemperatureValue relay;          // Main relay temperature
  @Nullable private TemperatureValue coolingSystem;  // Cooling system temperature
  @Nullable private TemperatureValue internal;       // General internal enclosure temperature

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