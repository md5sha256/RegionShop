package com.github.md5sha256.regionshop.util;

import cloud.commandframework.types.tuples.Pair;
import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {

    @Inject
    @Named("internal-logger")
    private Logger logger;
    @Inject
    private TaskChainFactory taskChainFactory;
    @Inject
    private MiniMessage miniMessage;

    private LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

    private volatile String prefix = "";

    public synchronized void setPrefix(String prefix) {
        if (prefix == null) {
            this.prefix = "";
        } else {
            this.prefix = prefix.concat(" ");
        }
    }

    public void log(@NotNull Level level, @NotNull String... messages) {
        final String[] serialized = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            final Component component = miniMessage.parse(prefix + messages[i]);
            serialized[i] = serializer.serialize(component);
        }
        taskChainFactory.newChain().sync(() -> {
            for (String s : serialized) {
                logger.log(level, s);
            }
        }).execute();
    }

    public void logException(@NotNull Exception exception) {
        taskChainFactory.newChain().sync(exception::printStackTrace).execute();
    }

    public @NotNull LogCollector newLogCollector() {
        return new LogCollector();
    }

    public class LogCollector {

        private final List<Pair<String[], Level>> toLog = new LinkedList<>();

        private LogCollector() {
        }

        public void log(@NotNull Level level, @NotNull String... messages) {
            toLog.add(Pair.of(Objects.requireNonNull(messages), level));
        }

        public void logException(@NotNull Exception exception) {
            logException(Level.SEVERE, exception);
        }

        public void logException(@NotNull Level level, @NotNull Exception exception) {
            final byte[] data;
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 final PrintWriter writer = new PrintWriter(outputStream);) {
                exception.printStackTrace(writer);
                data = outputStream.toByteArray();
            } catch (IOException ex) {
                // Should not happen!
                throw new RuntimeException(ex);
            }
            final String string = new String(data);
            final String[] split = string.split("\\n");
            toLog.add(Pair.of(split, level));
        }

        public List<Pair<String[], Level>> getAccumulated() {
            return new ArrayList<>(toLog);
        }

        public CompletableFuture<Void> dumpLog() {
            if (this.toLog.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            final List<Pair<String[], Level>> copy = new ArrayList<>(this.toLog);
            this.toLog.clear();
            // Force synchronization with main
            final CompletableFuture<Void> future = new CompletableFuture<>();
            taskChainFactory.newChain().sync(() -> {
                for (Pair<String[], Level> pair : copy) {
                    LogUtils.this.log(pair.getSecond(), pair.getFirst());
                }
                future.complete(null);
            }).execute();
            return future;
        }

    }

}
