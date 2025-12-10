package w3cp.model.iso15118;

import java.time.Instant;
import java.util.List;

/**
 * Represents ISO 15118 Plug & Charge provisioning and state reporting.
 * Used by both backend (for provisioning) and CP (for reporting state and CSR).
 */
public record Iso15118State(
    Instant timestamp,

    boolean enabled,              // Whether ISO 15118 Plug & Charge is active

    SeccSection secc,             // TLS certificate used by CP for authenticating to EV
    TrustRootsSection trustRoots // Root trust anchors used by CP and expected by EVs
) {

  /**
   * SECC certificate chain used by the CP during mTLS with the EV.
   * Backend may install a new chain; CP may return CSR.
   */
  public record SeccSection(
      List<String> installedSecc,     // SECC certificate chain currently installed in CP (PEM)
      List<String> toInstallSecc,     // SECC certificate chain to install (PEM, leaf to root)
      String csrPem                   // Optional CSR from CP (PEM)
  ) {}

  /**
   * Root trust anchors used to validate identities in ISO15118.
   * Sometimes referred to as MO Roots (trusted by CP) and V2G Roots (trusted by EV).
   */
  public record TrustRootsSection(
      List<String> trustedByCpRoots,   // Roots CP uses to verify EV contract certificates (PEM)
      List<String> trustedByCarRoots,  // Roots expected to be trusted by EV to verify CP cert (PEM)

      List<String> installTrustOnCp,   // Roots to install into CP trust store (PEM)
      List<String> installTrustOnCar,  // Roots to forward to EV as trusted (PEM)

      InstallMode cpInstallMode,       // Whether to merge or replace existing roots in CP
      InstallMode carInstallMode       // Whether to merge or replace EV trust roots
  ) {
    public enum InstallMode {
      addAndKeepExisting,   // Merge new roots with existing
      replaceExisting       // Replace all existing roots
    }
  }
}
