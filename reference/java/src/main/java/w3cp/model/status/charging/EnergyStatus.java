package w3cp.model.status.charging;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@Setter
public class EnergyStatus {

  // -------------------------------------------------------------------------
  // 1) CP-level cumulative meters (long-lived, reconciliation only)
  // -------------------------------------------------------------------------

  /**
   * Total active energy delivered from CP to vehicle (grid -> vehicle), in kWh.
   * Monotonically increasing over device lifetime (unless explicitly reset by maintenance).
   * Nullable; null = "not reported".
   */
  @Nullable private Double energyImportKWh;

  /**
   * Total active energy received by CP from vehicle (vehicle -> grid/CP), in kWh.
   * Monotonically increasing over device lifetime.
   * Nullable; null = "not reported".
   */
  @Nullable private Double energyExportKWh;

  /**
   * Optional reactive energy registers, if available (kvarh).
   * <p>
   * Reactive energy is the energy that oscillates back and forth between source and load
   * without doing useful work, but is necessary for maintaining voltage levels and magnetic
   * fields in AC systems. Important for power quality monitoring and grid compliance.
   * <p>
   * Nullable; null = "not reported".
   */
  @Nullable private Double reactiveEnergyImportKvarh;
  @Nullable private Double reactiveEnergyExportKvarh;

  // -------------------------------------------------------------------------
  // 2) Instantaneous electrical snapshot at ChargePointStatus.timestamp
  // -------------------------------------------------------------------------

  /**
   * Line voltage (implementation-defined: phase-to-neutral or phase-to-phase), in V.
   * Nullable; null = "not reported".
   */
  @Nullable private Double voltage;

  /**
   * Current per phase, in A, e.g. [L1, L2, L3].
   * Nullable; null or empty = "not reported".
   */
  @Nullable private List<Double> currentPerPhase;

  /**
   * Instantaneous active power from CP to vehicle (charging), in W (>= 0).
   * Nullable; null = "not reported".
   */
  @Nullable private Double activePowerImportW;

  /**
   * Instantaneous active power from vehicle to CP (discharging / V2G), in W (>= 0).
   * Nullable; null = "not reported".
   */
  @Nullable private Double activePowerExportW;

}