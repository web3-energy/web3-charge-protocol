package w3cp.model.status.charging;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class Connector {

  // ---------------------------------------------------------------------------
  // W3CP logical status
  // ---------------------------------------------------------------------------

  /**
   * High-level connector status from W3CP view.
   *
   * null and ConnectorStatus.unknown SHOULD be treated equivalently.
   */
  @Nullable private ConnectorStatus status;

  /**
   * Physical cable lock state (if the hardware has a lock).
   * null means "unknown / not reported".
   */
  @Nullable private Boolean locked;

  // ---------------------------------------------------------------------------
  // IEC 61851 physical state (Mode 3 control pilot)
  // ---------------------------------------------------------------------------

  /**
   * IEC 61851 control pilot state.
   *
   * Encodes both the letter (A–F) and its nominal CP voltage.
   * null and Iec61851State.unknown SHOULD be treated equivalently.
   */
  @Nullable private Iec61851State iec61851State;

  /**
   * PWM duty cycle on CP, if known.
   * Used for available current calculation and debugging.
   * Nullable; absence means "not reported".
   */
  @Nullable private Double pwmDutyCycle;

  /**
   * Main contactor / relay state, if known.
   *  - true  -> contactor closed (power path enabled)
   *  - false -> contactor open  (power path disabled)
   *  - null  -> not reported
   */
  @Nullable private Boolean relayClosed;

  /**
   * Edition of IEC 61851-1 implemented by the firmware.
   * null and Iec61851Edition.unknown SHOULD be treated equivalently.
   */
  @Nullable private Iec61851Edition iec61851Edition;

  /**
   * IEC 61851-1 charging mode for this connector.
   *
   * See IEC 61851-1 (2017 / 2025-draft):
   *  - mode1: AC from standard socket-outlet, no control pilot, no dedicated EVSE.
   *  - mode2: AC from socket-outlet with in-cable control and protection device (IC-CPD).
   *  - mode3: AC from dedicated EVSE with control pilot (typical wallbox/public AC).
   *  - mode4: DC from dedicated EVSE with control pilot (DC fast charging / HPC).
   *
   * null and Iec61851ChargingMode.unknown SHOULD be treated equivalently.
   */
  @Nullable private Iec61851ChargingMode chargingMode;

  /**
   * Directional energy capability / state reported by the Charge Point.
   *
   * This is NOT a live power measurement; it reflects how the CP is currently
   * configured or allowed to operate direction-wise.
   *
   * null and EnergyDirection.unknown SHOULD be treated equivalently.
   */
  @Nullable private EnergyDirection energyDirection;

  // ---------------------------------------------------------------------------
  // Physical connector description
  // ---------------------------------------------------------------------------

  /**
   * How the vehicle connects physically.
   *
   * null and InterfaceType.unknown SHOULD be treated equivalently.
   */
  @Nullable private InterfaceType interfaceType;

  /**
   * AC or DC energy path.
   *
   * null and CurrentType.unknown SHOULD be treated equivalently.
   */
  @Nullable private CurrentType currentType;

  /**
   * Connector family / standard (open-ended string).
   *
   * Recommended (non-exhaustive):
   *  - "type2"
   *  - "ccs2"
   *  - "ccs1"
   *  - "nacs"
   *  - "chademo"
   *  - "schuko"
   *  - "gbtAc" (GB/T AC)
   *  - "gbtDc" (GB/T DC)
   *  - "mcs"
   *  - "unknown"
   *
   * null SHOULD be treated like "unknown".
   */
  @Nullable private String connectorStandard;

  // ---------------------------------------------------------------------------
  // Enums
  // ---------------------------------------------------------------------------

  public enum ConnectorStatus {
    available,
    plugged,
    charging,
    faulted,
    unavailable,
    unknown
  }

  /**
   * IEC 61851 state including nominal CP voltage information.
   * Values are informational; you do NOT need to send these voltages separately.
   */
  public enum Iec61851State {

    /**
     * State A: no vehicle connected.
     * CP ≈ +12 V, no load.
     */
    a(12.0, 12.0, "no vehicle connected"),

    /**
     * State B: vehicle detected, not yet charging.
     * CP ≈ +9 V with resistor to PE.
     */
    b(9.0, 9.0, "vehicle detected, not ready"),

    /**
     * State C: vehicle ready/charging, no ventilation required.
     * CP ≈ +6 V with resistor to PE.
     */
    c(6.0, 6.0, "ready/charging, no ventilation required"),

    /**
     * State D: vehicle ready/charging, ventilation required.
     * CP ≈ +3 V with different resistor coding.
     */
    d(3.0, 3.0, "ready/charging, ventilation required"),

    /**
     * State E: error, no power available.
     * CP ≈ 0 V (e.g. EVSE off / error).
     */
    e(0.0, 0.0, "cp error / supply off"),

    /**
     * State F: CP fault (e.g. short to +12 V or to PE).
     * CP ≈ -12 V or other fault condition.
     */
    f(-12.0, -12.0, "cp fault / wiring error"),

    /**
     * Unknown or not reported.
     */
    unknown(null, null, "unknown / not reported");

    private final Double nominalCpVoltageMin;
    private final Double nominalCpVoltageMax;
    private final String description;

    Iec61851State(Double nominalCpVoltageMin, Double nominalCpVoltageMax, String description) {
      this.nominalCpVoltageMin = nominalCpVoltageMin;
      this.nominalCpVoltageMax = nominalCpVoltageMax;
      this.description = description;
    }

    public Double getNominalCpVoltageMin() {
      return nominalCpVoltageMin;
    }

    public Double getNominalCpVoltageMax() {
      return nominalCpVoltageMax;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Iec61851Edition {
    iec618511_2017,
    iec618511_2025Draft,
    unknown
  }

  public enum Iec61851ChargingMode {
    /**
     * Mode 1:
     * AC charging from a standard socket-outlet,
     * no control pilot, no dedicated EVSE.
     */
    mode1,

    /**
     * Mode 2:
     * AC charging from a socket-outlet with in-cable control and protection device (IC-CPD).
     */
    mode2,

    /**
     * Mode 3:
     * AC charging from a dedicated EVSE with control pilot per IEC 61851-1.
     */
    mode3,

    /**
     * Mode 4:
     * DC charging using a dedicated EVSE with control pilot (DC fast charging / HPC).
     */
    mode4,

    /**
     * Unknown or not reported by firmware.
     */
    unknown
  }

  /**
   * Directional energy capability/state reported by the Charge Point.
   *
   * This is NOT a live power reading; it expresses how the CP is currently
   * configured to operate:
   *
   *  - cpToVehicleOnly → CP can only send power to the vehicle.
   *  - vehicleToCpOnly → CP can only accept power from the vehicle.
   *  - bidirectional   → CP can both deliver and accept energy.
   */
  public enum EnergyDirection {
    cpToVehicleOnly,
    vehicleToCpOnly,
    bidirectional,
    unknown
  }

  public enum InterfaceType {
    plug,
    wireless,
    pantographTop,
    pantographBottom,
    rail,
    unknown
  }

  public enum CurrentType {
    ac,
    dc,
    unknown
  }
}