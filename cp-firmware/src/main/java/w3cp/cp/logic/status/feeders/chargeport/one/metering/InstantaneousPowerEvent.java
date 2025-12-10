package w3cp.cp.logic.status.feeders.chargeport.one.metering;

import java.util.List;

public record InstantaneousPowerEvent(
    Double voltage,
    List<Double> currentPerPhase,
    Double activePowerImportW,
    Double activePowerExportW
) implements MeteringEvent {
  
  @Override
  public void applyTo(MeteringRuntimeState state) {
    state.setVoltage(voltage);
    state.setCurrentPerPhase(currentPerPhase);
    state.setActivePowerImportW(activePowerImportW);
    state.setActivePowerExportW(activePowerExportW);
  }
}
