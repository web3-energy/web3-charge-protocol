package w3cp.model.identity;

import java.time.Instant;

/**
 * Message sent by the backend to challenge the Charge Point's identity.
 * <p>
 * Contains a unique nonce, the backend's current timestamp, and an optional
 * proof-of-work difficulty parameter. If difficulty > 0, the CP must perform
 * a hash computation that proves work was done.
 */
public record IdentityChallenge(
    String nonce,        // Random, fresh, single-use value to be signed by the CP
    Instant timestamp,   // Current backend time, used to help CP sync clocks
    int difficulty       // Proof-of-work level: number of trailing zero bits
                         // required in SHA-256 hash of IdentityProof payload
                         // 0 means PoW is not required e.g. any hash would do.
) {
}
