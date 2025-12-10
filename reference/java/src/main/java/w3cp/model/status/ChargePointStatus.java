package w3cp.model.status;

import lombok.Getter;
import lombok.Setter;

import w3cp.model.status.charging.ChargePort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

/**
 * Full status report from a Charge Point.
 * Sent after identity verification and periodically during operation.
 */
@Getter
@Setter
public class ChargePointStatus {
  @Nonnull private Instant timestamp;               // Time of this report
  @Nullable private Instant onlineSince;             // Time the CP connected and was verified
  @Nonnull private ConnectionType connectionType;   // Type of network connection
  @Nonnull private List<ChargePort> chargePorts;    // All reported charge ports
  @Nonnull private SystemInfo systemInfo;           // System and firmware information for this chargepoint.

  public enum ConnectionType {
    ethernet,
    wifi,
    lte,
    unknown
  }
}