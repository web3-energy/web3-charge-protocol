package w3cp.model.iso15118;

import java.time.Instant;

public record Iso15118Trigger(
    Instant timestamp,         // Time trigger was issued
    boolean signCsrTemplate,
    String csrTemplatePem     // Required: full CSR to be signed (PEM)
) {}
