package w3cp.cp.logic.status.feeders.chargeport.one;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.chargeport.ChargePortFeeder;
import io.smallrye.mutiny.Uni;
import w3cp.cp.logic.status.feeders.chargeport.one.session.*;
import w3cp.model.status.charging.ChargeSession;

import java.util.UUID;

@Slf4j
@ApplicationScoped
public class SessionSubFeeder implements ChargePortFeeder<ChargeSession> {

  private static final double POWER_THRESHOLD_W = 50.0;
  
  private final SessionRuntimeState state = new SessionRuntimeState();

  public synchronized void applyEvent(SessionEvent event) {
    switch (event) {
      case VehiclePluggedEvent e -> onVehiclePlugged(e);
      case VehicleUnpluggedEvent e -> onVehicleUnplugged(e);
      case PowerSampleEvent e -> onPowerSample(e);
    }
  }

  @Override
  public Uni<ChargeSession> fetch() {
    return Uni.createFrom().item(() ->
        state.hasActiveOrEndedSession() ? state.toSnapshotDto() : null
    );
  }

  private void onVehiclePlugged(VehiclePluggedEvent e) {
    if (state.hasActiveSession()) {
      log.warn("Vehicle plugged while session active, resetting previous session");
    }
    
    state.reset();
    state.setSessionId(UUID.randomUUID());
    state.setCreatedAt(e.at());
    state.setSessionState(ChargeSession.SessionState.pending);
    state.setLastUpdateAt(e.at());
    
    log.info("Session started: {}", state.getSessionId());
  }

  private void onVehicleUnplugged(VehicleUnpluggedEvent e) {
    if (!state.hasActiveSession()) {
      return;
    }
    
    state.setEndedAt(e.at());
    state.setSessionState(ChargeSession.SessionState.completed);
    state.setEndReason(ChargeSession.EndReason.vehicleUnplugged);
    state.setLastUpdateAt(e.at());
    
    log.info("Session ended: {}", state.getSessionId());
  }

  private void onPowerSample(PowerSampleEvent e) {
    if (!state.hasActiveSession()) {
      return;
    }
    
    state.setCurrentMeterImportKWh(e.meterImportKWh());
    state.setCurrentMeterExportKWh(e.meterExportKWh());
    
    boolean hasPower =
        Math.abs(e.importW()) > POWER_THRESHOLD_W ||
        Math.abs(e.exportW()) > POWER_THRESHOLD_W;
    
    if (hasPower) {
      if (state.getEnergyFlowStartedAt() == null) {
        state.setEnergyFlowStartedAt(e.at());
        state.setMeterImportAtStartKWh(e.meterImportKWh());
        state.setMeterExportAtStartKWh(e.meterExportKWh());
      }
      state.setSessionState(ChargeSession.SessionState.active);
    } else {
      if (state.getSessionState() == ChargeSession.SessionState.active) {
        state.setSessionState(ChargeSession.SessionState.paused);
      }
    }
    
    state.setLastUpdateAt(e.at());
  }
}
