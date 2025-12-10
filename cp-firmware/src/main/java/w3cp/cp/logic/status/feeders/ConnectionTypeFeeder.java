package w3cp.cp.logic.status.feeders;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.events.ConnectionOpenedEvent;
import w3cp.cp.util.NetworkDetectorUtil;
import w3cp.model.status.ChargePointStatus;

@Slf4j
@ApplicationScoped
public class ConnectionTypeFeeder implements Feeder<ChargePointStatus.ConnectionType> {

  private ChargePointStatus.ConnectionType connectionType;

  @PostConstruct
  void init() {
    refresh("initialization");
  }

  @ConsumeEvent("CONNECTION_OPENED")
  void onConnectionOpened(ConnectionOpenedEvent event) {
    refresh("CONNECTION_OPENED");
  }

  private void refresh(String reason) {
    try {
      var detected = NetworkDetectorUtil.detectConnectionType();
      this.connectionType = detected != null ? detected : ChargePointStatus.ConnectionType.unknown;
      log.info("✅ ConnectionTypeFeeder {}: {}", reason, this.connectionType);
    } catch (Exception e) {
      log.warn("Failed to detect connection type during {}", reason, e);
      if (this.connectionType == null) {
        this.connectionType = ChargePointStatus.ConnectionType.unknown;
      }
    }
  }

  @Override
  public Uni<ChargePointStatus.ConnectionType> fetch() {
    return Uni.createFrom().item(connectionType);
  }
}
