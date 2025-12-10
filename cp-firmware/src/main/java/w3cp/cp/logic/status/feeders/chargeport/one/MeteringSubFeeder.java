package w3cp.cp.logic.status.feeders.chargeport.one;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import w3cp.cp.logic.status.feeders.chargeport.ChargePortFeeder;
import w3cp.cp.logic.status.feeders.chargeport.one.metering.MeteringEvent;
import w3cp.cp.logic.status.feeders.chargeport.one.metering.MeteringRuntimeState;
import w3cp.cp.logic.status.feeders.chargeport.one.session.PowerSampleEvent;
import w3cp.model.status.charging.EnergyStatus;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class MeteringSubFeeder implements ChargePortFeeder<EnergyStatus> {

  @Inject
  SessionSubFeeder sessionFeeder;

  private final MeteringRuntimeState state = new MeteringRuntimeState();
  private final Object lock = new Object();

  @Override
  public Uni<EnergyStatus> fetch() {
    return Uni.createFrom().item(() -> {
      synchronized (lock) {
        return state.toSnapshotDto();
      }
    });
  }

  public void applyEvent(MeteringEvent event) {
    synchronized (lock) {
      event.applyTo(state);
      emitPowerSample();
    }
  }

  private void emitPowerSample() {
    Double importW = state.getActivePowerImportW();
    Double exportW = state.getActivePowerExportW();
    Double importKWh = state.getEnergyImportKWh();
    Double exportKWh = state.getEnergyExportKWh();
    
    if (importW != null && exportW != null && importKWh != null && exportKWh != null) {
      sessionFeeder.applyEvent(new PowerSampleEvent(
          Instant.now(),
          importW,
          exportW,
          importKWh,
          exportKWh
      ));
    }
  }
}
