package w3cp.model.diagnostics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

/**
 * WIP: Backend-initiated diagnostic window where the CP may override
 * selected feeders (car, IEC 61851, meter, RFID).
 *
 * Semantics:
 * - If a feeder DTO (car / iec61851 / meter / rfid) is PRESENT, the CP
 *   must use its 'mode' and provided fields to override that feeder
 *   for the duration of this session.
 * - If a feeder DTO is NULL, that feeder stays REAL (no override).
 * - CP must enforce a strict max TTL (e.g. 1 hour).
 *
 * Status messages remain truthful; only feeder inputs are overridden.
 */
@Getter
@Setter
@Builder
public class DiagnosticSession {

  /** Unique CP-local identifier for this diagnostic window. */
  private String diagnosticId;

  /** When diagnostic mode began. */
  private Instant startedAt;

  /** When diagnostic mode auto-expires (must be clamped <= CP max). */
  private Instant expiresAt;

  /** Always BACKEND in v1. */
  private StartedBy startedBy;

  /** Optional backend reference (request id, operator id, ticket id). */
  private String startedByRef;

  /**
   * Charge Port IDs affected by this diagnostic session.
   * Positive integers only. Null/empty = whole CP.
   */
  private Set<Integer> chargePortIds;

  /** Selected feeder overrides. */
  private Feeders feeders;

  /** Optional human-readable explanation. */
  private String reason;


    /* ------------------------------------------------------------
       Feeders container
       ------------------------------------------------------------ */

  @Getter
  @Setter
  @Builder
  public static class Feeders {

    /** Car / EV behaviour. */
    private CarFeeder car;

    /** IEC 61851 pilot / plug state machine. */
    private Iec61851Feeder iec61851;

    /** Meter + thermal simulation. */
    private MeterFeeder meter;

    /** RFID / external identifier simulation. */
    private RfidFeeder rfid;
  }


    /* ------------------------------------------------------------
       Car Feeder
       ------------------------------------------------------------ */

  @Getter
  @Setter
  @Builder
  public static class CarFeeder {

    /** REAL / SIMULATED / REPLAY. */
    private FeederMode mode;

    /** Scenario identifier understood by CP firmware. */
    private String scenarioId;

    /** Optional initial SoC (0–100). */
    private Integer initialSocPercent;

    /** Optional target SoC (0–100). */
    private Integer targetSocPercent;

    /** Optional requested energy in Wh. */
    private Integer requestedEnergyWh;

    /** Simulated auth type used by car. */
    private CarAuthMethod authMethod;
  }

  public enum CarAuthMethod {
    NONE,
    PNC_CONTRACT,
    EIM
  }


    /* ------------------------------------------------------------
       IEC 61851 Feeder
       ------------------------------------------------------------ */

  @Getter
  @Setter
  @Builder
  public static class Iec61851Feeder {

    private FeederMode mode;

    /** Scenario id for 61851 state sequences. */
    private String scenarioId;

    /** Initial IEC 61851 state (A/B/C/D). */
    private String initialState;
  }


    /* ------------------------------------------------------------
       Meter Feeder (updated with random ranges)
       ------------------------------------------------------------ */

  @Getter
  @Setter
  @Builder
  public static class MeterFeeder {

    private FeederMode mode;

    /** Optional starting energy counter in Wh. */
    private Long initialEnergyWh;

    /** Scenario id for meter evolution. */
    private String scenarioId;

    /* -------- Power simulation (random between range) -------- */

    /**
     * Minimum simulated power in W.
     * If both minPowerW and maxPowerW are set: random in [min..max].
     * If equal: fixed value.
     * If null: CP uses scenario or default.
     */
    private Integer minPowerW;
    private Integer maxPowerW;

    /* -------- Connector temperature (°C) -------- */

    private Integer minConnectorTempC;
    private Integer maxConnectorTempC;

    /* -------- PCB temperature (°C) -------- */

    private Integer minPcbTempC;
    private Integer maxPcbTempC;
  }


    /* ------------------------------------------------------------
       RFID Feeder
       ------------------------------------------------------------ */

  @Getter
  @Setter
  @Builder
  public static class RfidFeeder {

    private FeederMode mode;

    /** Scenario id (single tag, rapid swap, invalid, etc.). */
    private String scenarioId;

    /** Optional single tag to simulate. */
    private String tagId;
  }


    /* ------------------------------------------------------------
       Enums
       ------------------------------------------------------------ */

  public enum StartedBy {
    BACKEND
  }

  public enum FeederMode {
    REAL,
    SIMULATED,
    REPLAY
  }
}
