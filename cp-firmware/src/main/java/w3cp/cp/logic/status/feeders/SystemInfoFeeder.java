package w3cp.cp.logic.status.feeders;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.model.status.ChargePointStatus;
import w3cp.model.status.SystemInfo;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Instant;

@Slf4j
@ApplicationScoped
public class SystemInfoFeeder implements Feeder<SystemInfo> {

  @jakarta.inject.Inject
  SystemThermalInfoFeeder systemThermalInfoFeeder;

  private final Instant bootTime = Instant.now();

  @Override
  public Uni<SystemInfo> fetch() {
    return Uni.createFrom().item(() -> {
      SystemInfo info = new SystemInfo();

      info.setBootTime(bootTime);
      info.setFirmwareVersion("0.1");
      info.setFirmwareInstalledOn(bootTime);

      var os = (com.sun.management.OperatingSystemMXBean)
          ManagementFactory.getOperatingSystemMXBean();

      info.setCpuLoad(os.getCpuLoad());

      info.setMemoryTotalBytes(os.getTotalMemorySize());
      info.setMemoryFreeBytes(os.getFreeMemorySize());

      File root = new File("/");
      long total = root.getTotalSpace();
      long free = root.getUsableSpace();
      info.setDiskUsagePercent(total > 0 ? 
          100.0 * (total - free) / (double) total : null);

      info.setOsVersion(System.getProperty("os.name") + " " + System.getProperty("os.version"));
      info.setArchitecture(System.getProperty("os.arch"));

      return systemThermalInfoFeeder.fetch()
          .map(thermal -> {
            info.setThermalInfo(thermal);
            return info;
          })
          .await().indefinitely();
    });
  }
}
