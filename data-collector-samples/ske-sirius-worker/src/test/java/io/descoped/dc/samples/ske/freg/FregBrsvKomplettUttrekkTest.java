package io.descoped.dc.samples.ske.freg;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.api.Specification;
import io.descoped.dc.api.node.builder.SpecificationBuilder;
import io.descoped.dc.api.util.CommonUtils;
import io.descoped.dc.application.ssl.SecretManagerSSLResource;
import io.descoped.dc.core.executor.Worker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static io.descoped.dc.api.Builders.addContent;
import static io.descoped.dc.api.Builders.bodyPublisher;
import static io.descoped.dc.api.Builders.claims;
import static io.descoped.dc.api.Builders.console;
import static io.descoped.dc.api.Builders.context;
import static io.descoped.dc.api.Builders.eval;
import static io.descoped.dc.api.Builders.execute;
import static io.descoped.dc.api.Builders.get;
import static io.descoped.dc.api.Builders.headerClaims;
import static io.descoped.dc.api.Builders.jqpath;
import static io.descoped.dc.api.Builders.jwt;
import static io.descoped.dc.api.Builders.jwtToken;
import static io.descoped.dc.api.Builders.nextPage;
import static io.descoped.dc.api.Builders.paginate;
import static io.descoped.dc.api.Builders.parallel;
import static io.descoped.dc.api.Builders.post;
import static io.descoped.dc.api.Builders.publish;
import static io.descoped.dc.api.Builders.security;
import static io.descoped.dc.api.Builders.sequence;
import static io.descoped.dc.api.Builders.status;
import static io.descoped.dc.api.Builders.statusCode;
import static io.descoped.dc.api.Builders.whenVariableIsNull;

public class FregBrsvKomplettUttrekkTest {

    static final SpecificationBuilder specificationBuilder = Specification.start("SKE-FREG-BRSV-UTTREKK-BATCH", "Collect FREG BRSV Bulk Uttrekk", "maskinporten-jwt-grant")
            .configure(context()
                            .topic("freg-brsv-uttrekk-komplett")
//                    .variable("clientId", "${ENV.'ssb.ske.freg.test.clientId'}")
                            .variable("clientId", "${ENV.'ssb.ske.freg.prod.clientId'}")
                            .variable("jwtGrantTimeToLiveInSeconds", "${ENV.'ssb.jwtGrant.expiration'}")
                            .variable("ProduksjonURL", "https://folkeregisteret.api.skatteetaten.no/folkeregisteret")
                            .variable("fromFeedSequence", "0")
//                    .variable("jobId", "a4ec090c-a649-4fb2-bbd0-c64f9e522f7b")
                            .variable("nextBatch", "0")
            )
            .configure(security()
                            .identity(jwt("maskinporten",
                                            headerClaims()
                                                    .alg("RS256")
//                                    .x509CertChain("ssb-p12-test-certs"),
                                                    .x509CertChain("ske-p12-certs"),
                                            claims()
//                                    .audience("https://ver2.maskinporten.no/")
                                                    .audience("https://maskinporten.no/")
//                                    .issuer("${testClientId}")
                                                    .issuer("${clientId}")
                                                    .claim("scope", "folkeregister:deling/svalbardregister folkeregister:deling/offentligmedhjemmel")
                                                    .timeToLiveInSeconds("${jwtGrantTimeToLiveInSeconds}")
                                    )
                            )
            )
            .function(post("maskinporten-jwt-grant")
//                    .url("https://ver2.maskinporten.no/token/v1/token")
                            .url("https://maskinporten.no/token/v1/token")
                            .data(bodyPublisher()
                                    .urlEncoded(jwtToken()
                                            .identityId("maskinporten")
                                            .bindTo("JWT_GRANT")
                                            .token("grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${JWT_GRANT}")
                                    )
                            )
                            .validate(status().success(200))
                            .pipe(execute("create-job")
//                            .pipe(execute("loop")
                                            .inputVariable("accessToken", jqpath(".access_token"))
                            )
            )
            .function(get("create-job")
                    .url("${ProduksjonURL}/api/brsv/v1/uttrekk/komplett?feedsekvensnr=${fromFeedSequence}")
                    .header("Authorization", "Bearer ${accessToken}")
                    .validate(status().success(200))
                    .pipe(console())
                    .pipe(execute("loop")
                            .requiredInput("accessToken")
                            .requiredInput("jobId")
                            .inputVariable("jobId", jqpath(".jobbId"))
                    )
            )
            .function(paginate("loop")
                    .variable("fromBatch", "${nextBatch}")
                    .iterate(execute("batch-list")
                            .requiredInput("accessToken")
                            .requiredInput("jobId")
                            .requiredInput("fromBatch")
                    )
                    .prefetchThreshold(100_000)
                    .until(whenVariableIsNull("nextBatch"))
            )
            .function(get("batch-list")
                            .url("${ProduksjonURL}/api/brsv/v1/uttrekk/${jobId}/batch/${fromBatch}")
                            .header("Authorization", "Bearer ${accessToken}")
                            .retryWhile(statusCode().is(404, TimeUnit.SECONDS, 15))
                            .validate(status().success(200))
//                    .pipe(console())
                            .pipe(sequence(jqpath(".dokumentidentifikator[]"))
                                    .expected(jqpath("."))
                            )
                            .pipe(nextPage().output("nextBatch", eval("${cast.toLong(fromBatch) + 1}")))
                            .pipe(parallel(jqpath(".dokumentidentifikator[]"))
                                    .variable("position", jqpath("."))
                                    .pipe(addContent("${position}", "entry"))
                                    .pipe(execute("person-document")
                                            .requiredInput("accessToken")
                                            .inputVariable("personDocumentId", jqpath("."))
                                    )
                                    .pipe(publish("${position}"))
                            )
                            .returnVariables("nextBatch")
            )
            .function(get("person-document")
                    .url("${ProduksjonURL}/api/brsv/v1/personer/arkiv/${personDocumentId}?part=historikk")
                    .header("Authorization", "Bearer ${accessToken}")
                    .header("Accept", "application/xml")
                    .validate(status().success(200))
                    .pipe(addContent("${position}", "person"))
            );

    @Disabled
    @Test
    public void fregUttrekkBatch() {
        Worker.newBuilder()
                .configuration(DynamicConfiguration.builder()
                        .classpath("application-override.properties") // gitignored
                        .value("data.collector.worker.threads", "20")
                        .value("content.stream.connector", "rawdata")
                        .value("rawdata.client.provider", "filesystem")
                        .value("filesystem.storage-folder", "target/avro/rawdata-store")
                        .value("local-temp-folder", "target/avro/temp")
                        .value("avro-file.max.seconds", "60")
                        .value("avro-file.max.bytes", "67108864")
                        .value("avro-file.sync.interval", "20")
                        .value("listing.min-interval-seconds", "0")
                        .value("gcs.bucket-name", "")
                        .value("gcs.listing.min-interval-seconds", "30")
                        .value("gcs.service-account.key-file", "")
                        .environment("DC_")
                        .build()
                        .asMap())
                .buildCertificateFactory(Paths.get("/Volumes/SSB BusinessSSL/certs"))
                .printConfiguration()
                .specification(specificationBuilder)
                .stopAtNumberOfIterations(25000)
                .build()
                .run();
    }

    @Disabled
    @Test
    public void fregUttrekkBatchWithSecretManager() {
        DynamicConfiguration securityConfiguration = DynamicConfiguration.builder()
                .classpath("application-override.properties")
                .build();

        DynamicConfiguration configuration = DynamicConfiguration.builder()
                .value("data.collector.worker.threads", "20")
                .value("content.stream.connector", "rawdata")
                .value("rawdata.client.provider", "filesystem")
                .value("filesystem.storage-folder", "target/avro/rawdata-store")
                .value("local-temp-folder", "target/avro/temp")
                .value("avro-file.max.seconds", "60")
                .value("avro-file.max.bytes", "67108864")
                .value("avro-file.sync.interval", "20")
                .value("listing.min-interval-seconds", "0")
                .value("gcs.bucket-name", "")
                .value("gcs.listing.min-interval-seconds", "30")
                .value("gcs.service-account.key-file", "")
                .value("data.collector.sslBundle.provider", "google-secret-manager")
                .value("data.collector.sslBundle.gcp.projectId", "descoped-team")
                .value("data.collector.sslBundle.gcp.serviceAccountKeyPath", GoogleSecretManagerIntegrationTest.getServiceAccountFile(securityConfiguration))
                .value("data.collector.sslBundle.type", "p12")
                .value("data.collector.sslBundle.name", "ske-prod-certs")
                .value("data.collector.sslBundle.archiveCertificate", "ssb-prod-p12-certificate")
                .value("data.collector.sslBundle.passphrase", "ssb-prod-p12-passphrase")
                .value("rawdata.encryption.provider", "google-secret-manager")
                .value("rawdata.encryption.gcp.serviceAccountKeyPath", GoogleSecretManagerIntegrationTest.getServiceAccountFile(securityConfiguration))
                .value("rawdata.encryption.gcp.projectId", "descoped-team")
                .value("rawdata.encryption.key", "rawdata-prod-freg-encryption-key")
                .value("rawdata.encryption.salt", "rawdata-prod-freg-encryption-salt")
                .environment("DC_")
                .build();

        Worker.newBuilder()
                .configuration(configuration.asMap())
                .useBusinessSSLResourceSupplier(() -> new SecretManagerSSLResource(configuration))
                .printConfiguration()
                .specification(specificationBuilder)
                .stopAtNumberOfIterations(25000)
                .build()
                .run();
    }

    @Disabled
    @Test
    public void writeTargetConsumerSpec() throws IOException {
        Path currentPath = CommonUtils.currentPath().getParent().getParent();
        Path targetPath = currentPath.resolve("data-collection-consumer-specifications");

        boolean targetProjectExists = targetPath.toFile().exists();
        if (!targetProjectExists) {
            throw new RuntimeException(String.format("Couldn't locate '%s' under currentPath: %s%n", targetPath.toFile().getName(), currentPath.toAbsolutePath().toString()));
        }

        Files.writeString(targetPath.resolve("specs").resolve("ske-freg-brsv-bulk-uttrekk-spec.json"), specificationBuilder.serialize());
    }
}
