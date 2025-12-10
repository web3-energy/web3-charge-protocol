package w3cp.cp.logic.status.feeders.chargeport.one.metering;

public record EnergyMeterUpdateEvent(
    Double energyImportKWh,
    Double energyExportKWh
) implements MeteringEvent {
  
  @Override
  public void applyTo(MeteringRuntimeState state) {
    if (energyImportKWh != null) {
      state.setEnergyImportKWh(energyImportKWh);
    }
    if (energyExportKWh != null) {
      state.setEnergyExportKWh(energyExportKWh);
    }
  }
}
