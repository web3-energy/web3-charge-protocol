package w3cp.cp.logic.status;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.CPConnection;
import w3cp.cp.logic.W3CPJson;
import w3cp.model.W3CPMessage;
import w3cp.model.W3CPMessageType;
import w3cp.model.status.ChargePointStatus;
import w3cp.model.status.charging.ChargePort;
import w3cp.model.status.charging.Connector;
import w3cp.model.status.charging.EnergyStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@ApplicationScoped
public class CPState {

  @Inject
  CPConnection connection;

  @Inject
  ChargePointStatusOrchestrator orchestrator;

  private final AtomicReference<ChargePointStatus> lastObservedStatus = new AtomicReference<>();
  private final AtomicReference<Instant> lastStatusTransmission = new AtomicReference<>(null);

  private static final long MAX_STATUS_INTERVAL_SECONDS = 300;
  private static final double ENERGY_DELTA_THRESHOLD_KWH = 0.1; // ~50s at 7.4kW
  private static final double POWER_THRESHOLD_W = 50.0;

  @Scheduled(every = "1s")
  void evaluateAndMaybeSend() {
    if (!connection.isVerified()) {
      return;
    }

    try {
      orchestrator.assembleStatus()
          .subscribe().with(
              this::handleNewStatusSnapshot,
              error -> {
                if (!isShutdownError(error)) {
                  log.error("Failed to assemble status", error);
                }
              }
          );
    } catch (Exception e) {
      if (!isShutdownError(e)) {
        log.error("Error in evaluateAndMaybeSend", e);
      }
    }
  }

  private void handleNewStatusSnapshot(ChargePointStatus current) {
    ChargePointStatus previousObserved = lastObservedStatus.getAndSet(current);
    Instant lastSentTime = lastStatusTransmission.get();

    boolean timeoutExceeded = lastSentTime == null ||
        Instant.now().isAfter(lastSentTime.plusSeconds(MAX_STATUS_INTERVAL_SECONDS));

    boolean significantChange = isSignificantChange(previousObserved, current);

    if (!significantChange && !timeoutExceeded) {
      return;
    }

    sendStatus(current)
        .subscribe().with(
            unused -> {
              lastStatusTransmission.set(Instant.now());
              log.debug("Status sent (significantChange={}, timeoutExceeded={})",
                  significantChange, timeoutExceeded);
            },
            error -> {
              if (!isShutdownError(error)) {
                log.error("Failed to send status", error);
              }
            }
        );
  }

  private boolean isSignificantChange(ChargePointStatus prevObserved,
                                      ChargePointStatus current) {
    if (prevObserved == null) {
      return true;
    }

    if (!Objects.equals(prevObserved.getConnectionType(), current.getConnectionType())) {
      return true;
    }

    if (!Objects.equals(prevObserved.getOnlineSince(), current.getOnlineSince())) {
      return true;
    }

    if (prevObserved.getChargePorts() == null || current.getChargePorts() == null) {
      return !Objects.equals(prevObserved.getChargePorts(), current.getChargePorts());
    }

    if (prevObserved.getChargePorts().size() != current.getChargePorts().size()) {
      return true;
    }

    // TODO: match charge ports by ID instead of index if ordering becomes unstable
    for (int i = 0; i < prevObserved.getChargePorts().size(); i++) {
      ChargePort prevPort = prevObserved.getChargePorts().get(i);
      ChargePort currPort = current.getChargePorts().get(i);

      if (isChargePortSignificantChange(prevPort, currPort)) {
        return true;
      }
    }

    return false;
  }

  private boolean isChargePortSignificantChange(ChargePort prev, ChargePort curr) {
    if (prev == null || curr == null) {
      return !Objects.equals(prev, curr);
    }

    Connector prevConn = prev.getConnector();
    Connector currConn = curr.getConnector();

    if (prevConn == null || currConn == null) {
      if (!Objects.equals(prevConn, currConn)) {
        return true;
      }
    } else {
      if (!Objects.equals(prevConn.getStatus(), currConn.getStatus())) {
        return true;
      }

      if (!Objects.equals(prevConn.getIec61851State(), currConn.getIec61851State())) {
        return true;
      }

      if (!Objects.equals(prevConn.getRelayClosed(), currConn.getRelayClosed())) {
        return true;
      }

      if (!Objects.equals(prevConn.getLocked(), currConn.getLocked())) {
        return true;
      }
    }

    EnergyStatus prevMeter = prev.getMetering();
    EnergyStatus currMeter = curr.getMetering();

    if (prevMeter == null || currMeter == null) {
      if (!Objects.equals(prevMeter, currMeter)) {
        return true;
      }
      return false;
    }

    Double prevEnergy = prevMeter.getEnergyImportKWh();
    Double currEnergy = currMeter.getEnergyImportKWh();

    if (prevEnergy != null && currEnergy != null) {
      if (Math.abs(currEnergy - prevEnergy) >= ENERGY_DELTA_THRESHOLD_KWH) {
        return true;
      }
    }

    Double prevPower = prevMeter.getActivePowerImportW();
    Double currPower = currMeter.getActivePowerImportW();

    boolean prevCharging = prevPower != null && prevPower > POWER_THRESHOLD_W;
    boolean currCharging = currPower != null && currPower > POWER_THRESHOLD_W;

    if (prevCharging != currCharging) {
      return true;
    }

    return false;
  }

  private boolean isShutdownError(Throwable t) {
    if (t == null) return false;
    String msg = t.getMessage();
    return msg != null && (
        msg.contains("Error Occurred After Shutdown") ||
        msg.contains("ArcContainer") ||
        msg.contains("SmallRyeContextManager")
    );
  }

  private Uni<Void> sendStatus(ChargePointStatus status) {
    W3CPMessage<ChargePointStatus> message = new W3CPMessage<>(
        W3CPMessageType.chargepointStatus,
        status,
        null, null
    );

    try {
      String json = W3CPJson.MAPPER.writeValueAsString(message);
      return connection.send(json);
    } catch (Exception e) {
      return Uni.createFrom().failure(e);
    }
  }
}
