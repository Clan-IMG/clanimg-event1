package net.clanimg.clanimgEvent1.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;

public class WorldCloner {

    private final Plugin plugin;

    public WorldCloner(Plugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<World> cloneAndLoad(String templateWorldName, String newWorldName) {
        CompletableFuture<World> future = new CompletableFuture<>();
        Path source = Bukkit.getWorldContainer().toPath().resolve(templateWorldName);
        Path target = Bukkit.getWorldContainer().toPath().resolve(newWorldName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                copyDirectory(source, target);
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(plugin, () -> future.completeExceptionally(e));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World world = new WorldCreator(newWorldName).createWorld();
                    if (world == null) {
                        future.completeExceptionally(new IllegalStateException("createWorld() lieferte null fuer " + newWorldName));
                    } else {
                        future.complete(world);
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        });

        return future;
    }

    public void unloadAndDelete(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }

        Path target = Bukkit.getWorldContainer().toPath().resolve(worldName);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                deleteDirectory(target);
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte Instanzwelt '" + worldName + "' nicht loeschen: " + e.getMessage());
            }
        });
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("Template-Weltordner nicht gefunden: " + source);
        }

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (name.equals("session.lock") || name.equals("uid.dat")) {
                    return FileVisitResult.CONTINUE;
                }
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
