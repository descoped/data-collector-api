package io.descoped.dc.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.application.server.Configurations;
import io.descoped.dc.application.server.UndertowApplication;
import io.descoped.dc.application.ssl.BusinessSSLResourceSupplier;
import io.descoped.dc.application.ssl.SecretManagerSSLResource;
import io.descoped.dc.core.metrics.MetricsAgent;
import io.descoped.dc.core.util.JavaUtilLoggerBridge;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.function.Supplier;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private static final ConfigurationOverride configurationOverride = ConfigurationOverride.NONE;

    public static void main(String[] args) {
        MetricsAgent.premain(null, ByteBuddyAgent.install());
        long now = System.currentTimeMillis();

        String logbackConfigurationFile = System.getenv("LOGBACK_CONFIGURATION_FILE");
        if (logbackConfigurationFile != null) {
            System.setProperty("log4j.configurationFile", logbackConfigurationFile);
        }
        JavaUtilLoggerBridge.installJavaUtilLoggerBridgeHandler();
        if (logbackConfigurationFile != null) {
            LOG.debug("Using logback configuration: {}", logbackConfigurationFile);
        }

        DynamicConfiguration.Builder configurationBuilder = DynamicConfiguration.builder()
                .classpath("application-defaults.properties")
                .file(Path.of("/conf/application-defaults.properties"))
                .file(Path.of("/conf/application.properties"))
                .file(Path.of("/conf/application-override.properties"));
        /*
         * pre-req: `docker-compose up` in this repo
         */
        if (configurationOverride == ConfigurationOverride.DEBUG_USING_POSTGRES) {
            configurationBuilder.classpath("application-integration.properties");
        }

        /*
         * pre-req: refer to data-collection-consumer-specifications-repo and configure gcs
         */
        if (configurationOverride == ConfigurationOverride.DEBUG_USING_GCS) {
            configurationBuilder.values(IntegrationTestProfile.loadApplicationPropertiesFromConsumerSpecificationProfile("gcs",
                    "data.collector.certs.directory", "gcs.service-account.key-file", "local-temp-folder"));
        }

        configurationBuilder
                .environment("DC_")
                .systemProperties();

        DynamicConfiguration declared = configurationBuilder.build();
        EnvironmentOverrides.requireMatched(System.getenv(), declared.asMap().keySet());
        DynamicConfiguration configuration = Configurations.withoutBlankValues(declared);

        /*
         * Delegated certificate loader from Google Secret Manager is placed here, because we don't won't a
         * dependency in the data collector to any host aware libraries such as the Google Secret Manager.
         * The supplier is wrapped by the BusinessSSLResourceSupplier and passes an implementing instance of
         * BusinessSSLBundle to Worker.useBusinessSSLResourceSupplier() in the Core module.
         *
         * Please note: only Google Secret Manager is supported!
         */

        String businessSslResourceProvider = configuration.find("data.collector.sslBundle.provider").orElse(null);
        Supplier<SecretManagerSSLResource> sslResourceSupplier = () -> new SecretManagerSSLResource(configuration);

        UndertowApplication application = UndertowApplication.initializeUndertowApplication(configuration,
                businessSslResourceProvider != null ? new BusinessSSLResourceSupplier(sslResourceSupplier) : null);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.warn("ShutdownHook triggered..");
                application.stop();
            }));

            application.start();

            long time = System.currentTimeMillis() - now;
            LOG.info("Server started in {}ms..", time);

            // wait for termination signal
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
            }
        } finally {
            application.stop();
        }
    }

    enum ConfigurationOverride {
        NONE,
        DEBUG_USING_POSTGRES,
        DEBUG_USING_GCS;
    }
}
