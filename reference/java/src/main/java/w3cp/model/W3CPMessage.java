package w3cp.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Generic wrapper for all messages in W3CP protocol
public record W3CPMessage<T>(
    @Nonnull W3CPMessageType type,         // Logical message type (e.g. identityProof, heartbeat, command)
    @Nonnull T payload,                    // Message content; canonical JSON of this is used for hash/signature
    @Nullable String payloadSignature,    // Signature over SHA-256(payload-bytes), Base64URL (no padding)
    @Nullable String payloadSha256Hash     // SHA-256 of canonical JSON payload, Base64URL (no padding)
) {
}
