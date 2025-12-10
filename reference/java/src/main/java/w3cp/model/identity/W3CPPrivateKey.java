package w3cp.model.identity;

/**
 * ⚠️ WARNING:
 * This structure exists only for test/simulator purposes.
 * Real Charge Points MUST use secure key storage (HSM, TPM, secure enclave).
 * Never expose private keys.
 */
public record W3CPPrivateKey(
    W3CPPublicKey.KeyType type,
    W3CPPublicKey.KeyEncoding encoding,
    String value // Usually a base64url-encoded PKCS#8 private key
) {
}
