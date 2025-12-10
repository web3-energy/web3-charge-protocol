package w3cp.model.identity.discovery;

import java.time.Instant;
import java.util.UUID;

/**
 * Sent by the backend to request the Charge Point to report all
 * identities it controls. Backend is responsible for when to allow
 * this message in unverified connections, if at all.
 */
public record IdentityDiscovery(
    UUID correlationId,
    Instant timestamp
) {}
