package w3cp.model.status;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;

@Getter
@Setter
public class ConnectorInfo {
  @Nullable private ConnectorStatus status;
  private boolean pluggedIn;
  @Nullable private String pluggedConnector;
  private boolean locked;
  @Nullable private List<String> connectors;

  public enum ConnectorStatus {
    available,
    plugged,
    charging,
    faulted,
    unavailable,
    unknown
  }
}