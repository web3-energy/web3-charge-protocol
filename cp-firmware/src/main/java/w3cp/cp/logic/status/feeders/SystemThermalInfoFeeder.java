package w3cp.cp.logic.status.feeders;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.model.status.SystemThermalInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class SystemThermalInfoFeeder implements Feeder<SystemThermalInfo> {

  private static double simulatedBase = 42.0;

  @Override
  public Uni<SystemThermalInfo> fetch() {
    return Uni.createFrom().item(() -> {
      Map<String, Double> sensors = readAllZoneTemperatures();
      if (sensors.isEmpty()) {
        sensors = simulateThermals();
      }

      SystemThermalInfo info = new SystemThermalInfo();
      
      if (!sensors.isEmpty()) {
        mapSensorsToSystemThermalInfo(sensors, info);
      }

      return info;
    });
  }

  private void mapSensorsToSystemThermalInfo(Map<String, Double> sensors, SystemThermalInfo info) {
    for (Map.Entry<String, Double> entry : sensors.entrySet()) {
      String name = entry.getKey().toLowerCase();
      Double temp = entry.getValue();
      
      if (name.contains("ambient")) {
        info.setAmbient(createTempValue(temp));
      } else if (name.contains("mcu") || name.contains("cpu") || name.contains("pkg") || name.contains("soc")) {
        info.setMcu(createTempValue(temp));
      } else if (name.contains("pcb") || name.contains("board")) {
        info.setPcb(createTempValue(temp));
      } else if (name.contains("internal")) {
        info.setInternal(createTempValue(temp));
      } else if (name.contains("sim")) {
        if (name.contains("controller")) {
          info.setMcu(createTempValue(temp));
        } else if (name.contains("board")) {
          info.setPcb(createTempValue(temp));
        } else if (name.contains("ambient")) {
          info.setAmbient(createTempValue(temp));
        }
      } else if (info.getInternal() == null) {
        info.setInternal(createTempValue(temp));
      }
    }
  }

  private SystemThermalInfo.TemperatureValue createTempValue(Double celsius) {
    SystemThermalInfo.TemperatureValue temp = new SystemThermalInfo.TemperatureValue();
    temp.setValue(celsius);
    temp.setUnit(SystemThermalInfo.TemperatureUnit.celsius);
    return temp;
  }

  private Map<String, Double> readAllZoneTemperatures() {
    try {
      Path thermalBase = Paths.get("/sys/class/thermal");
      if (!Files.exists(thermalBase)) {
        return Map.of();
      }

      try (Stream<Path> zones = Files.list(thermalBase)) {
        return zones
            .filter(p -> p.getFileName().toString().startsWith("thermal_zone"))
            .map(zone -> {
              String name = readZoneType(zone);
              Double temp = readZoneTemperature(zone);
              return temp != null && temp > 0 ? Map.entry(name, temp) : null;
            })
            .filter(e -> e != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Double::max
            ));
      }
    } catch (IOException e) {
      return Map.of();
    }
  }

  private String readZoneType(Path zonePath) {
    try {
      Path typeFile = zonePath.resolve("type");
      if (Files.exists(typeFile)) {
        return Files.readString(typeFile).trim();
      }
    } catch (IOException ignored) {}
    return zonePath.getFileName().toString();
  }

  private Double readZoneTemperature(Path zonePath) {
    try {
      Path tempFile = zonePath.resolve("temp");
      if (!Files.exists(tempFile)) {
        return null;
      }
      
      String content = Files.readString(tempFile).trim();
      long milliCelsius = Long.parseLong(content);
      return milliCelsius / 1000.0;
    } catch (IOException | NumberFormatException e) {
      return null;
    }
  }

  private Map<String, Double> simulateThermals() {
    simulatedBase += (Math.random() - 0.5);
    simulatedBase = Math.max(35.0, Math.min(65.0, simulatedBase));

    return Map.of(
        "controller_sim", simulatedBase,
        "board_sim", simulatedBase - 3,
        "ambient_sim", simulatedBase - 10
    );
  }
}
