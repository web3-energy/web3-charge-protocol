package w3cp.model.identity.discovery;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import w3cp.model.identity.key.PublicKeyIdentity;
import w3cp.model.identity.x509.X509Identity;
import w3cp.model.identity.web3.Web3Identity;

/** CP → Backend: reports available identities grouped by type. */
public record IdentityReport(
    UUID correlationId,
    Instant timestamp,
    List<PublicKeyIdentity> publicKeys,
    List<X509Identity> x509Certificates,
    List<Web3Identity> web3Identities
) {}