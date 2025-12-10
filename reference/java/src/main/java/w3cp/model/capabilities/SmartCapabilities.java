package w3cp.model.capabilities;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * Declares all smart capabilities and configuration of a charge point.
 * This is the full state/status reported by the CP.
 *
 * Wire format conventions (JSON):
 * - DayOfWeek:
 *     UPPERCASE enum name, e.g. "MONDAY", "TUESDAY", ..., "SUNDAY"
 * - LocalTime:
 *     Local time of the CP, ISO-8601, pattern "HH:mm:ss"
 *     Examples: "07:00:00", "17:30:00"
 * - Instant (timestamps):
 *     ISO-8601 instant in UTC or with offset, recommended "yyyy-MM-dd'T'HH:mm:ss'Z'"
 *     Example: "2025-01-31T17:00:00Z"
 *
 * Semantics:
 * - Fields named "supported" and "max*" are CP-owned capability declarations.
 *   Backend must not override them.
 * - Other fields are configuration/policy that the backend MAY change when supported == true.
 */
@Getter
@Setter
public class SmartCapabilities {

  /**
   * Handles firmware installation and update tracking.
   */
  private FirmwareUpdate firmwareUpdate;

  /**
   * Allows the backend to configure current limits on the CP.
   */
  private PowerLimiting powerLimiting;

  /**
   * Enables backend-triggered factory resets with defined modes.
   */
  private FactoryReset factoryReset;

  /**
   * Describes how the CP handles charging transactions while offline.
   */
  private OfflineTransactions offlineTransactions;

  /**
   * Allows CP to stream logs live to the backend.
   */
  private LogStreaming logStreaming;

  /**
   * Captures and streams key log lines around a critical error.
   */
  private CriticalErrorSnapshot criticalErrorSnapshot;

  /**
   * Allows secure backend access to CP internals via reverse SSH tunnel.
   */
  private ReverseSsh reverseSsh;

  /**
   * Unified smart charging: backend-managed time windows,
   * randomized delays, and energy-source restrictions.
   *
   * If supported == false, CP must ignore any smartCharging configuration.
   */
  private SmartCharging smartCharging;

  // ========================================================================
  // Basic capabilities
  // ========================================================================

  @Getter
  @Setter
  public static class PowerLimiting {
    private boolean supported;
    private Integer limitSumAllPhases; // current effective limit in W (backend-configurable if supported)
    private Integer limitPhase1;
    private Integer limitPhase2;
    private Integer limitPhase3;
    private Integer maxLimitPerPhase; // hardware max per phase (CP-owned upper bound)
    private int phasesAvailable;      // 1, 2, or 3 (CP-owned)
  }

  @Getter
  @Setter
  public static class FactoryReset {
    private boolean supported;
    private ResetType[] supportedTypes; // e.g. immediate, onIdle, manualConfirmation (CP-owned)
  }

  public enum ResetType {
    immediate,
    onIdle,
    manualConfirmation
  }

  @Getter
  @Setter
  public static class OfflineTransactions {
    private boolean supported;

    /**
     * Backend policy for RFID cards when offline.
     */
    private RfidMode rfidMode;

    /**
     * Plug & Charge (eMAID) is always online only.
     */
    private EmaidMode emaidMode;

    /**
     * Maximum number of sessions the CP will buffer while offline.
     * Backend should not configure policies that exceed this limit.
     */
    private int maxOfflineSessions;
  }

  public enum RfidMode {
    acceptAll,
    blockAll,
    replayKnownElseAccept,
    replayKnownElseBlock
  }

  /**
   * ISO 15118 Plug & Charge (eMAID) requires online validation.
   * Offline use is not technically possible or permitted.
   * Therefore, only rejectAll is allowed.
   */
  public enum EmaidMode {
    rejectAll
  }

  // ========================================================================
  // Logging capabilities
  // ========================================================================

  /**
   * Enables real-time log forwarding from the CP to the backend, with optional filtering.
   */
  @Getter
  @Setter
  public static class LogStreaming {
    private boolean supported;

    /**
     * If empty, CP decides what logs are streamed.
     * Optional support for filters (e.g., level, module).
     */
    private List<LogFilterType> supportedFilterTypes;

    /**
     * Whether the CP is actively streaming logs to backend.
     */
    private boolean currentlyStreaming;

    /**
     * Instant when current log stream began.
     *
     * Wire format: ISO-8601 instant, e.g. "2025-01-31T17:00:00Z"
     */
    private Instant startedStreaming;

    /**
     * Maximum duration (in hours) the stream may run.
     * Recommended default: 1
     */
    private int maximalStreamingDurationInHours;

    public enum LogFilterType {
      trace,
      debug,
      info,
      warn,
      error,
      fatal
    }
  }

  /**
   * Sends a log snapshot surrounding the moment of a critical failure.
   */
  @Getter
  @Setter
  public static class CriticalErrorSnapshot {
    private boolean supported;

    /**
     * Number of log lines to capture before the critical error line.
     */
    private int linesBefore;

    /**
     * Number of log lines to capture after the critical error line.
     */
    private int linesAfter;

    /**
     * If true, CP will actively capture and report snapshots when errors occur.
     */
    private boolean enabled;
  }

  // ========================================================================
  // Reverse SSH capabilities
  // ========================================================================

  /**
   * Declares CP support for establishing a reverse SSH tunnel to the backend,
   * enabling secure remote access to diagnostics or UI endpoints.
   */
  @Getter
  @Setter
  public static class ReverseSsh {
    private boolean supported;

    /**
     * Maximum allowed session duration in seconds. Example: 900
     * CP-owned safety limit.
     */
    private int maxSessionSeconds;

    /**
     * True if a session is currently active and accessible.
     */
    private boolean available;

    /**
     * SSH user to authenticate as. Example: "cp-client"
     */
    private String sshUser;

    /**
     * The hostname or IP address the CP should connect to for reverse SSH.
     * Example: "ssh.backend.example.com"
     */
    private String targetHost;

    /**
     * Port on the backend where reverse SSH service is exposed.
     * Example: 2222
     */
    private int targetPort;

    /**
     * Optional identifier for tracking the session (e.g., cp123).
     * Example: "wallbox-004"
     */
    private String sessionId;

    /**
     * Duration the current or last tunnel was up (seconds). Optional.
     */
    private Integer lastTunnelUptimeSeconds;

    /**
     * Instant of last successful session, if available.
     *
     * Wire format: ISO-8601 instant, e.g. "2025-01-31T17:00:00Z"
     */
    private Instant lastSessionTimestamp;

    /**
     * Optional reason for last failure, if applicable.
     */
    private String lastFailureReason;
  }

  // ========================================================================
  // Smart charging (merged RandomizedDelay + EnergyRestrictedCharging)
  // ========================================================================

  /**
   * Enables smart charging time windows with:
   * - randomized charging delays (jitter)
   * - energy input mode (solar/grid)
   * - solar usage limits
   *
   * If supported == false, CP must ignore the windows and behave as today.
   */
  @Getter
  @Setter
  public static class SmartCharging {
    private boolean supported;

    /**
     * Smart charging windows.
     *
     * First matching window wins. Overlapping windows are allowed;
     * precedence is list order (index 0 = highest priority).
     */
    private List<SmartChargingWindow> windows;
  }

  /**
   * A single time window with optional randomized delay and energy restrictions.
   *
   * All time fields are in CP local time.
   */
  @Getter
  @Setter
  public static class SmartChargingWindow {

    /**
     * Day of week this window applies to.
     * If null, applies to all days.
     *
     * Wire format: "MONDAY", "TUESDAY", ..., "SUNDAY"
     */
    private DayOfWeek day;

    /**
     * Start of window (inclusive), CP local time.
     *
     * Wire format: "HH:mm:ss"
     * Example: "17:00:00"
     */
    private LocalTime start;

    /**
     * End of window (exclusive), CP local time.
     *
     * Wire format: "HH:mm:ss"
     * Example: "20:00:00"
     *
     * If start == null and end == null: applies the whole day.
     */
    private LocalTime end;

    /**
     * If > 0, the CP MUST apply a random delay in [0, randomizedDelaySeconds]
     * (uniform distribution is recommended) for each charging session that
     * starts inside this window.
     *
     * If null or 0, no randomized delay for this window.
     */
    private Integer randomizedDelaySeconds;

    /**
     * Charging mode used during this window.
     * If null, CP must default to solarAndGrid.
     */
    private EnergyInputMode energyInput = EnergyInputMode.solarAndGrid;

    /**
     * Restrict CP to use only this percent of available solar (0–100).
     * If null, no explicit solar cap applies.
     */
    private Integer maxSolarUsagePercent;
  }

  public enum EnergyInputMode {
    solarAndGrid,
    matchSolarOutput
  }

  // ========================================================================
  // Firmware update capabilities
  // ========================================================================

  /**
   * Describes the firmware update process and current installation state.
   */
  @Getter
  @Setter
  public static class FirmwareUpdate {
    private boolean supported;
    private boolean supportsHttpFirmwareDownload;
    private boolean supportsInlineFirmwarePush;

    /**
     * Currently running firmware version on the CP.
     */
    private String currentFirmwareVersion;

    /**
     * Firmware version to be installed or in installation.
     */
    private String installFirmwareVersion;

    private InstallationLog installationLog;
  }

  /**
   * Full installation lifecycle state: download, validation, and actual flashing.
   */
  @Getter
  @Setter
  public static class InstallationLog {
    private DownloadPhase download;
    private ValidationPhase validation;
    private InstallationPhase installation;
  }

  /**
   * Metadata and progress state of the firmware file download step.
   */
  @Getter
  @Setter
  public static class DownloadPhase {
    private DownloadStatus status;
    private long downloadedSizeBytes;
    private long totalSizeBytes;
  }

  public enum DownloadStatus {
    downloading,
    success,
    failed
  }

  /**
   * Records integrity and authenticity checks performed on the downloaded firmware.
   */
  @Getter
  @Setter
  public static class ValidationPhase {
    /**
     * Overall validation status: idle, validating, success, failed, skipped.
     */
    private ValidationStatus status;
    private String expectedSha256Hash;
    private String computedSha256Hash;
    private boolean checksumValidated;
    private boolean signatureValidated;

    /**
     * Base64-encoded digital signature over expectedSha256Hash.
     */
    private String digitalSignatureOverHash;
  }

  public enum ValidationStatus {
    idle,
    validating,
    success,
    failed,
    skipped
  }

  /**
   * Tracks the final step of firmware installation on the CP.
   */
  @Getter
  @Setter
  public static class InstallationPhase {
    private InstallationStatus status;
    private String detail;
  }

  public enum InstallationStatus {
    idle,
    installing,
    success,
    failed
  }
}
