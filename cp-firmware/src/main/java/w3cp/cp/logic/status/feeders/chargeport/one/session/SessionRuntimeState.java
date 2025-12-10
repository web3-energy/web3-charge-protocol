package w3cp.cp.logic.status.feeders.chargeport.one.session;

import lombok.Getter;
import lombok.Setter;
import w3cp.model.status.charging.ChargeSession;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class SessionRuntimeState {
  private UUID sessionId;
  private Instant createdAt;
  private Instant energyFlowStartedAt;
  private Instant lastUpdateAt;
  private Instant endedAt;
  
  private Double meterImportAtStartKWh;
  private Double meterExportAtStartKWh;
  
  private ChargeSession.SessionState sessionState;
  private ChargeSession.EndReason endReason;

  public boolean hasActiveSession() {
    return sessionId != null && endedAt == null;
  }

  public boolean hasActiveOrEndedSession() {
    return sessionId != null;
  }

  public void reset() {
    sessionId = null;
    createdAt = null;
    energyFlowStartedAt = null;
    lastUpdateAt = null;
    endedAt = null;
    meterImportAtStartKWh = null;
    meterExportAtStartKWh = null;
    sessionState = null;
    endReason = null;
  }

  private Double currentMeterImportKWh;
  private Double currentMeterExportKWh;

  public ChargeSession toSnapshotDto() {
    ChargeSession dto = new ChargeSession();
    dto.setSessionId(sessionId);
    dto.setCreatedAt(createdAt);
    dto.setEnergyFlowStartedAt(energyFlowStartedAt);
    dto.setLastUpdateAt(lastUpdateAt);
    dto.setEndedAt(endedAt);
    dto.setSessionState(sessionState);
    dto.setEndReason(endReason);
    
    if (meterImportAtStartKWh != null && currentMeterImportKWh != null) {
      dto.setEnergyToVehicleKWh(currentMeterImportKWh - meterImportAtStartKWh);
    }
    if (meterExportAtStartKWh != null && currentMeterExportKWh != null) {
      dto.setEnergyFromVehicleKWh(currentMeterExportKWh - meterExportAtStartKWh);
    }
    
    return dto;
  }
}
