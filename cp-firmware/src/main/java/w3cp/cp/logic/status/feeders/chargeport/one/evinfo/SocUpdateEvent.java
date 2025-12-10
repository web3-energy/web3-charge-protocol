package w3cp.cp.logic.status.feeders.chargeport.one.evinfo;

public record SocUpdateEvent(Double soc) implements EvInfoEvent {
  
  @Override
  public void applyTo(EvInfoRuntimeState state) {
    if (state.getEvInfo() != null && state.getEvInfo().getEnergy() != null && soc != null) {
      state.getEvInfo().getEnergy().setSoc(soc.intValue());
    }
  }
}
