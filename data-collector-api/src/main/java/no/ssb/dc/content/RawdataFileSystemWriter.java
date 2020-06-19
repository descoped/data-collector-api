package no.ssb.dc.content;

import no.ssb.dc.api.content.ContentStream;
import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamConsumer;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class RawdataFileSystemWriter {

    private static final Logger LOG = LoggerFactory.getLogger(RawdataFileSystemWriter.class);
    private static final int TIMEOUT = 250; // seconds
    private static final long NAP = 250L; // millis

    private final ContentStreamConsumer consumer;
    private final Path rootPath;
    private final AtomicReference<Thread> workerThread = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean forceShutdown = new AtomicBoolean(false);
    private final Object lock = new Object();

    public RawdataFileSystemWriter(ContentStream contentStream, String topic, Path rootPath) {
        this.consumer = contentStream.consumer(topic);
        this.rootPath = rootPath.resolve(topic);
    }

    private void nap(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private Runnable worker() {
        TikaConfig config = TikaConfig.getDefaultConfig();
//        Detector detector = config.getDetector();
        List<MediaType> textMediaTypes = List.of("plain/text", "application/json", "application/xml").stream().map(MediaType::parse).collect(Collectors.toList());
        Metadata metadata = new Metadata();

        AutoDetectParser parser = new AutoDetectParser(config);
        Detector detector = parser.getDetector();

        return () -> {
            running.set(true);
            LOG.info("Consuming topic {} in {}", consumer.topic(), consumer.getClass());
            ContentStreamBuffer message;
            try {
                while (!closed.get() || !Thread.currentThread().isInterrupted()) {
                    while (!closed.get() && (message = consumer.receive(TIMEOUT, TimeUnit.MILLISECONDS)) != null) {
                        Path filePath = rootPath.resolve(message.position()).normalize();
                        if (!Files.exists(filePath)) {
                            Files.createDirectories(filePath);
                        }
                        for (String key : message.keys()) {
                            byte[] data = message.get(key);
                            String content = new String(data, StandardCharsets.UTF_8);
                            MediaType mediaType = detector.detect(new ByteArrayInputStream(data), metadata);
                            String subtype = mediaType.getSubtype();
                            if ("plain".equals(subtype)) {
                                if (content.startsWith("{") || content.startsWith("[") && !key.endsWith(".json")) {
                                    subtype = ".json";
                                } else if (content.startsWith("<")){
                                    subtype = ".xml";
                                } else {
                                    subtype = "";
                                }
                            } else {
                                subtype = "." + subtype;
                            }
                            Path contentFilePath = filePath.resolve(filePath.resolve(key) + subtype);
                            Files.write(contentFilePath, data);
                        }
                        if (closed.get()) {
                            break;
                        }
                    }
                    if (closed.get()) {
                        break;
                    }
                    nap(NAP);
                }

                running.set(false);
                closed.set(false);
                forceShutdown.set(false);
                workerThread.set(null);

            } catch (InterruptedException | IOException e) {
                running.set(false);
                closed.set(false);
                workerThread.set(null);
                if (!(forceShutdown.get() && e instanceof InterruptedException)) {
                    throw new RuntimeException(e);
                }
                forceShutdown.set(false);
            }
        };
    }

    public void start() {
        if (!isRunning() && !isClosed() && workerThread.get() == null) {
            synchronized (lock) {
                LOG.info("Starting dump service..");
                closed.set(false);
                workerThread.set(new Thread(worker()));
                workerThread.get().start();
            }
        }
    }

    public void shutdown() {
        if (isRunning() && !isClosed() && workerThread.get() != null) {
            synchronized (lock) {
                LOG.info("Shutdown dump service!");
                closed.set(true);
                nap(TIMEOUT);
                if (isRunning()) {
                    LOG.info("Force Shutdown dump service!");
                    forceShutdown.set(true);
                    nap(TIMEOUT);
                    if (isRunning()) {
                        workerThread.get().interrupt();
                    }
                }
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isClosed() {
        return closed.get();
    }
}
