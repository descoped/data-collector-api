package no.ssb.dc.content;

import no.ssb.rawdata.api.RawdataClient;
import no.ssb.rawdata.api.RawdataConsumer;
import no.ssb.rawdata.api.RawdataMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RawdataFileSystemWriter {

    private static final Logger LOG = LoggerFactory.getLogger(RawdataFileSystemWriter.class);
    private static final int TIMEOUT = 1 * 1000; // seconds
    private static final long NAP = 500L; // millis

    private final RawdataConsumer consumer;
    private final Path rootPath;
    private final AtomicReference<Thread> workerThread = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean forceShutdown = new AtomicBoolean(false);
    private final Object lock = new Object();

    public RawdataFileSystemWriter(RawdataClient rawdataClient, String topic, Path rootPath) {
        this.consumer = rawdataClient.consumer(topic);
        this.rootPath = rootPath.resolve(topic);
    }

    private void nap(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private Runnable worker() {
        return () -> {
            running.set(true);
            RawdataMessage message;
            try {
                while (!closed.get() || !Thread.currentThread().isInterrupted()) {
                    while (!closed.get() && (message = consumer.receive(TIMEOUT, TimeUnit.MILLISECONDS)) != null) {
                        Path filePath = rootPath.resolve(message.position()).normalize();
                        if (!Files.exists(filePath)) {
                            Files.createDirectories(filePath);
                        }
                        for (String key : message.keys()) {
                            Path contentFilePath = filePath.resolve(filePath.resolve(key));
                            LOG.trace("Dump file: {}", contentFilePath.toAbsolutePath().toString());
                            Files.write(contentFilePath, message.get(key));
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
