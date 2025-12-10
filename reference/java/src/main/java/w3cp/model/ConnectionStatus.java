package w3cp.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Sent by the backend to indicate the result of the identity verification process.
 * Or disconnect.
 * Or error.
 * If status is not 'verified', the backend will close the connection.
 */
public record ConnectionStatus(
    @Nonnull Status status,  // Connection state result
    @Nullable String reason  // Optional reason for failure or error (nullable)
) {

  public enum Status {
    verified,       // ✅ Connection successfully verified
    disconnected,   //    ❌ Identity rejected or forcefully terminated
    error           // ⚠️ Protocol violation or internal backend error
  }
}
