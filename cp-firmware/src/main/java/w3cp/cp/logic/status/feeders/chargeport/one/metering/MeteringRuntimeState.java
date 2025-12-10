package w3cp.cp.logic.status.feeders.chargeport.one.metering;

import lombok.Getter;
import lombok.Setter;
import w3cp.model.status.charging.EnergyStatus;

import java.util.List;

@Getter
@Setter
public class MeteringRuntimeState {
  private Double energyImportKWh;
  private Double energyExportKWh;
  private Double reactiveEnergyImportKvarh;
  private Double reactiveEnergyExportKvarh;
  private Double voltage;
  private List<Double> currentPerPhase;
  private Double activePowerImportW;
  private Double activePowerExportW;

  public EnergyStatus toSnapshotDto() {
    EnergyStatus energy = new EnergyStatus();
    energy.setEnergyImportKWh(energyImportKWh);
    energy.setEnergyExportKWh(energyExportKWh);
    energy.setReactiveEnergyImportKvarh(reactiveEnergyImportKvarh);
    energy.setReactiveEnergyExportKvarh(reactiveEnergyExportKvarh);
    energy.setVoltage(voltage);
    energy.setCurrentPerPhase(currentPerPhase);
    energy.setActivePowerImportW(activePowerImportW);
    energy.setActivePowerExportW(activePowerExportW);
    return energy;
  }
}
