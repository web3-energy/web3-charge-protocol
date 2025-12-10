package w3cp.model.identity.x509;

/**
 * X.509 identity using PEM-encoded certificate string.
 */
public record X509Identity(
    String certificatePem
) {}
