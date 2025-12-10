package w3cp.model.status;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.Instant;

@Getter
@Setter
public class SystemInfo {
  @Nullable private String firmwareVersion;            // Current firmware version string
  @Nullable private Instant firmwareInstalledOn;       // When this firmware was installed

  @Nullable private Instant bootTime;                  // Timestamp of last system boot
  @Nullable private Double cpuLoad;                    // CPU load average (0.0–1.0)
  @Nullable private Long memoryFreeBytes;              // Free RAM in bytes
  @Nullable private Long memoryTotalBytes;             // Total RAM in bytes
  @Nullable private Double diskUsagePercent;           // Disk usage percentage (0–100)
  @Nullable private String osVersion;                  // e.g., "Linux 6.1.52", "baremetal"
  @Nullable private String architecture;               // e.g., "armv8", "x86_64"

  @Nullable private Boolean ethernetReady;             // Ethernet interface is physically up and usable
  @Nullable private Boolean wifiReady;                 // Wi-Fi interface is connected to an AP
  @Nullable private Boolean lteReady;                  // LTE modem is registered and has a data connection

  @Nullable private SystemThermalInfo thermalInfo;     // System-wide thermal telemetry
}