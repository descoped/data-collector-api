package io.descoped.dc.server.content;

import io.descoped.config.DynamicConfiguration;
import io.descoped.rawdata.api.RawdataClient;
import io.descoped.rawdata.api.RawdataClientInitializer;
import io.descoped.rawdata.api.RawdataConsumer;
import io.descoped.rawdata.api.RawdataMessage;
import io.descoped.service.provider.api.ProviderConfigurator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExportRawdataStreamTest {

    final static Logger LOG = LoggerFactory.getLogger(ExportRawdataStreamTest.class);

    final static DynamicConfiguration configuration = DynamicConfiguration.builder()
            .value("rawdata.client.provider", "postgres")
            .value("data.collector.worker.threads", "20")
            .value("postgres.driver.host", "localhost")
            .value("postgres.driver.port", "15432")
            .value("postgres.driver.user", "rdc")
            .value("postgres.driver.password", "rdc")
            .value("postgres.driver.database", "rdc")
            .value("rawdata.postgres.consumer.prefetch-size", "100")
            .value("rawdata.postgres.consumer.prefetch-poll-interval-when-empty", "1000")
            .value("local-temp-folder", "target/_tmp_avro_")
            .value("avro-file.max.seconds", "86400")
            .value("avro-file.max.bytes", "67108864")
            .value("avro-file.sync.interval", "524288")
            .value("gcs.bucket-name", "")
            .value("gcs.listing.min-interval-seconds", "30")
            .value("gcs.service-account.key-file", "")
            .environment("DC_")
            .build();

    @Disabled
    @Test
    void export() {
        RawdataClient rawdataClient = ProviderConfigurator.configure(RawdataClientInitializer.class, configuration.find("rawdata.client.provider").orElse(null), configuration.asMap());
        assertNotNull(rawdataClient);

        try (RawdataConsumer consumer = rawdataClient.consumer("sirius-person-utkast")) {
            RawdataMessage message;
            while ((message = consumer.receive(1, TimeUnit.SECONDS)) != null) {
                Map<String, byte[]> data = message.data();
                byte[] manifest = data.get("manifest.json");
                byte[] entry = data.get("entry");

                LOG.info("{}: {}", message.position(), new String(entry));

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
