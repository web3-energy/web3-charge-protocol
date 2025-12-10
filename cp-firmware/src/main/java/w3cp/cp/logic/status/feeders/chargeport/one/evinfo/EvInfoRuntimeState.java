package w3cp.cp.logic.status.feeders.chargeport.one.evinfo;

import lombok.Getter;
import lombok.Setter;
import w3cp.model.status.charging.EvInfo;

@Getter
@Setter
public class EvInfoRuntimeState {
  private EvInfo evInfo;

  public void reset() {
    evInfo = null;
  }

  public boolean hasEvInfo() {
    return evInfo != null;
  }

  public EvInfo toSnapshotDto() {
    return evInfo;
  }
}
