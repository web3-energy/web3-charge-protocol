package w3cp.cp.logic.status;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.ConnectionTypeFeeder;
import w3cp.cp.logic.status.feeders.OnlineSinceFeeder;
import w3cp.cp.logic.status.feeders.SystemInfoFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.ChargePortOrchestratorFeeder;
import w3cp.model.status.ChargePointStatus;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class ChargePointStatusOrchestrator {

  @Inject
  SystemInfoFeeder systemInfoFeeder;

  @Inject
  ConnectionTypeFeeder connectionTypeFeeder;

  @Inject
  OnlineSinceFeeder onlineSinceFeeder;

  @Inject
  ChargePortOrchestratorFeeder chargePortFeeder;

  public Uni<ChargePointStatus> assembleStatus() {
    return Uni.combine().all().unis(
        systemInfoFeeder.fetch(),
        connectionTypeFeeder.fetch(),
        onlineSinceFeeder.fetch(),
        chargePortFeeder.fetch()
    ).asTuple().map(tuple -> {
      ChargePointStatus status = new ChargePointStatus();
      status.setTimestamp(Instant.now());
      status.setSystemInfo(tuple.getItem1());
      status.setConnectionType(tuple.getItem2());
      status.setOnlineSince(tuple.getItem3());
      status.setChargePorts(java.util.List.of(tuple.getItem4()));
      return status;
    });
  }
}
