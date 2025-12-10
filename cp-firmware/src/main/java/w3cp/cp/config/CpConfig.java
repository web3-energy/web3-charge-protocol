package w3cp.cp.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "w3cp.cp")
public interface CpConfig {
  @WithName("cp-id")
  String cpId();

  @WithName("identity-type")
  String identityType();

  @WithName("charge-ports")
  List<ChargePortConfig> chargePorts();

  interface ChargePortConfig {
    @WithName("charge-port-id")
    int chargePortId();

  }

}


