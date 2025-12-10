package w3cp.cp.logic.status.feeders.chargeport.one.evinfo;

public record EnergyUpdateEvent(Double energyWh) implements EvInfoEvent {
  
  @Override
  public void applyTo(EvInfoRuntimeState state) {
    if (state.getEvInfo() != null && state.getEvInfo().getEnergy() != null && energyWh != null) {
      state.getEvInfo().getEnergy().setEnergyWh(energyWh);
    }
  }
}
