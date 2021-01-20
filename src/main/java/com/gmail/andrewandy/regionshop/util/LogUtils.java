package com.gmail.andrewandy.regionshop.util;

import cloud.commandframework.types.tuples.Pair;
import com.gmail.andrewandy.regionshop.RegionShop;
import com.google.inject.Inject;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import sun.rmi.runtime.Log;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {

    @Inject
    private Logger logger;
    @Inject
    private RegionShop plugin;
    @Inject
    private BukkitAudiences audiences;
    @Inject
    private MiniMessage miniMessage;
    @Inject
    private BungeeComponentSerializer serializer;

    public void log(@NotNull Level level, @NotNull String... messages) {
        final String[] serialized = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            final Component component = miniMessage.parse(messages[i]);
            final BaseComponent[] bungee = serializer.serialize(component);
            serialized[i] = BaseComponent.toLegacyText(bungee);
        }
        if (Bukkit.isPrimaryThread()) {
            for (String s : serialized) {
                logger.log(level, s);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (String s : serialized) {
                    logger.log(level, s);
                }
            });
        }
    }

    public void logException(@NotNull Exception exception) {
        if (Bukkit.isPrimaryThread()) {
            exception.printStackTrace();
        } else {
            Bukkit.getScheduler().runTask(plugin, (Runnable) exception::printStackTrace);
        }
    }

    public @NotNull LogCollector newLogCollector() {
        return new LogCollector();
    }

    public class LogCollector {

        private LogCollector() {
        }

        private final List<Pair<String[], Level>> toLog = new LinkedList<>();

        public void log(@NotNull Level level, @NotNull String... messages) {
            toLog.add(Pair.of(Objects.requireNonNull(messages), level));
        }

        public void logException(@NotNull Exception exception) {
            logException(Level.SEVERE, exception);
        }

        public void logException( @NotNull Level level, @NotNull Exception exception) {
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
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Pair<String[], Level> pair : copy) {
                    LogUtils.this.log(pair.getSecond(), pair.getFirst());
                }
                future.complete(null);
            });
            return future;
        }

    }

}
