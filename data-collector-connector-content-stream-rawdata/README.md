# Data Collector Connector: Rawdata Content Stream

Content store provider `rawdata`: dc publishes content to [rawdata-client](https://github.com/descoped/rawdata-client-project)
topics, optionally encrypted. The rawdata provider dc ships with (`filesystem`, `postgres`, `gcs`, and the built-in `memory` and `discard`) is chosen
with `rawdata.client.provider`.

JPMS module: `io.descoped.dc.content.rawdata`. Part of [Data Collector](../README.md); built and released with the
whole repository.

## Content encryption

Content is encrypted when both of these are set:

```properties
rawdata.encryption.key=PASSPHRASE
rawdata.encryption.salt=SALT
```

The key is derived with PBKDF2-HMAC-SHA256 (65 536 iterations) and content is encrypted with AES-256 through
[encryption-util](https://github.com/descoped/encryption-util) 3. A consumer reads it with encryption-util 3 and a key
derived the same way from the same password and salt (encryption-util 3's own derivation refuses salts shorter than
16 bytes). The key and salt can also come from Google Secret Manager; see
[Administration](../docs/administration.md).

- [Administration](../docs/administration.md)
- [Architecture](../docs/architecture.md)
