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
import static io.descoped.dc.api.Builders.context;
import static io.descoped.dc.api.Builders.eval;
import static io.descoped.dc.api.Builders.execute;
import static io.descoped.dc.api.Builders.get;
import static io.descoped.dc.api.Builders.jqpath;
import static io.descoped.dc.api.Builders.nextPage;
import static io.descoped.dc.api.Builders.paginate;
import static io.descoped.dc.api.Builders.parallel;
import static io.descoped.dc.api.Builders.publish;
import static io.descoped.dc.api.Builders.security;
import static io.descoped.dc.api.Builders.sequence;
import static io.descoped.dc.api.Builders.status;
import static io.descoped.dc.api.Builders.statusCode;
import static io.descoped.dc.api.Builders.whenVariableIsNull;

public class FregKomplettUttrekkTest {

    static final SpecificationBuilder specificationBuilder = Specification.start("SKE-FREG-UTTREKK-BATCH", "Collect FREG Bulk Uttrekk", "create-job")
            .configure(context()
                            .topic("freg-uttrekk-komplett")
                            .variable("ProduksjonURL", "https://folkeregisteret.api.skatteetaten.no/folkeregisteret/offentlig-med-hjemmel")
                            .variable("fromFeedSequence", "0")
//                    .variable("jobId", "c5d3af2f-4ac7-4506-af67-61e71c5f0b8f")
                            .variable("nextBatch", "0")
            )
            .configure(security()
                    .sslBundleName("ske-prod-certs")
            )
            .function(get("create-job")
                    .url("${ProduksjonURL}/api/v1/uttrekk/komplett?feedsekvensnr=${fromFeedSequence}")
                    .validate(status().success(200))
                    .pipe(execute("loop")
                            .requiredInput("jobId")
                            .inputVariable("jobId", jqpath(".jobbId"))
                    )
            )
            .function(paginate("loop")
                    .variable("fromBatch", "${nextBatch}")
                    .iterate(execute("batch-list")
                            .requiredInput("jobId")
                            .requiredInput("fromBatch")
                    )
                    .prefetchThreshold(100_000)
                    .until(whenVariableIsNull("nextBatch"))
            )
            .function(get("batch-list")
                    .url("${ProduksjonURL}/api/v1/uttrekk/${jobId}/batch/${fromBatch}")
                    .retryWhile(statusCode().is(404, TimeUnit.SECONDS, 15))
                    .validate(status().success(200))
                    .pipe(sequence(jqpath(".dokumentidentifikator[]"))
                            .expected(jqpath("."))
                    )
                    .pipe(nextPage().output("nextBatch", eval("${cast.toLong(fromBatch) + 1}")))
                    .pipe(parallel(jqpath(".dokumentidentifikator[]"))
                            .variable("position", jqpath("."))
                            .pipe(addContent("${position}", "entry"))
                            .pipe(execute("person-document")
                                    .inputVariable("personDocumentId", jqpath("."))
                            )
                            .pipe(publish("${position}"))
                    )
                    .returnVariables("nextBatch")
            )
            .function(get("person-document")
                    .url("${ProduksjonURL}/api/v1/personer/arkiv/${personDocumentId}?part=person-basis&part=identitetsgrunnlag-utvidet&part=relasjon-utvidet&part=utlendingsmyndighetenesIdentifikasjonsnummer&part=innflytting&part=utflytting&part=foedselINorge&part=opphold&part=forholdTilSametingetsValgmanntall")
                    .header("Accept", "application/xml")
                    .validate(status().success(200))
                    .pipe(addContent("${position}", "person"))
            );

    @Disabled
    @Test
    public void fregUttrekkBatch() {
        Worker.newBuilder()
                .configuration(DynamicConfiguration.builder()
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

        Files.writeString(targetPath.resolve("specs").resolve("ske-freg-bulk-uttrekk-spec.json"), specificationBuilder.serialize());
    }
}
