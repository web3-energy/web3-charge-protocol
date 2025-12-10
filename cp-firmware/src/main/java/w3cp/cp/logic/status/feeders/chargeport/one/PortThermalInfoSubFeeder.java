package w3cp.cp.logic.status.feeders.chargeport.one;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.chargeport.ChargePortFeeder;
import io.smallrye.mutiny.Uni;
import w3cp.model.status.charging.PortThermalInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class PortThermalInfoSubFeeder implements ChargePortFeeder<PortThermalInfo> {

  private static double simulatedBase = 42.0;

  @Override
  public Uni<PortThermalInfo> fetch() {
    return Uni.createFrom().item(() -> new PortThermalInfo());
  }


}
