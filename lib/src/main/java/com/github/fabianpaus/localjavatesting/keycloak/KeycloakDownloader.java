package com.github.fabianpaus.localjavatesting.keycloak;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KeycloakDownloader {

    public static Path download(String version, Path parentDirectory) {
        Path downloadDir = parentDirectory.resolve("keycloak-" + version);
        if (alreadyDownloaded(downloadDir)) {
            System.out.println("Keycloak: Using downloaded version at " + downloadDir);
            cleanupDataDirectory(downloadDir);
            cleanupProvidersDirectory(downloadDir);
            return downloadDir.toAbsolutePath();
        }

        long startTime = System.currentTimeMillis();
        System.out.println("Keycloak: Downloading version " + version);

        String keycloakZipUrl = "https://github.com/keycloak/keycloak/releases/download/"
                + version + "/keycloak-" + version + ".zip";
        unzip(keycloakZipUrl, parentDirectory);


        Path absDownloadDir = downloadDir.toAbsolutePath();
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        System.out.println("Keycloak: Finished downloading (" + duration + "s) to " + absDownloadDir);

        return absDownloadDir;
    }

    /**
     * Delete all files inside the data/h2 directory.
     * This resets any persistent database changes made via the Keycloak.
     *
     * @param home Home directory containing the Keycloak distribution
     */
    public static void cleanupDataDirectory(Path home) {
        Path dataDir = home.resolve("data/h2");
        deleteAllFiles(dataDir);
    }

    /**
     * Delete all files in the providers directory.
     * This cleans up any old JARs from previous test runs with potentially different versions.
     *
     * @param home Home directory containing the Keycloak distribution
     */
    public static void cleanupProvidersDirectory(Path home) {
        Path providersDir = home.resolve("providers");
        deleteAllFiles(providersDir);
    }

    private static void deleteAllFiles(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            files.forEach((path) -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Checks whether the specified directory contains a downloaded Keycloak distribution.
     *
     * @param homeDirectory Directory to check
     * @return true if the directory contains a Keycloak distribution
     */
    private static boolean alreadyDownloaded(Path homeDirectory) {
        if (!Files.isDirectory(homeDirectory)) {
            return false;
        }
        if (!Files.isRegularFile(homeDirectory.resolve("bin/kc.bat"))) {
            return false;
        }
        if (!Files.isRegularFile(homeDirectory.resolve("lib/quarkus-run.jar"))) {
            return false;
        }
        return Files.isRegularFile(homeDirectory.resolve("lib/app/keycloak.jar"));
    }

    /**
     * Download and unzip a file from a remote URL.
     * @param url URL to download the ZIP file from
     * @param destinationDirectory Directory to unzip the files into
     */
    private static void unzip(String url, Path destinationDirectory) {
        try {
            URL zipUrl = new URI(url).toURL();
            try (ZipInputStream zip = new ZipInputStream(zipUrl.openStream())) {

                Files.createDirectories(destinationDirectory);

                byte[] buffer = new byte[8192];

                ZipEntry zipEntry = zip.getNextEntry();
                while (zipEntry != null) {
                    Path newFile = newFile(destinationDirectory, zipEntry);

                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Path parent = newFile.getParent();
                        if (!Files.isDirectory(parent)) {
                            Files.createDirectories(parent);
                        }

                        try (OutputStream fos = Files.newOutputStream(newFile)) {
                            int length;
                            while ((length = zip.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    zipEntry = zip.getNextEntry();
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path newFile(Path destinationDir, ZipEntry zipEntry) throws IOException {
        Path destFile = destinationDir.resolve(zipEntry.getName());

        String destDirPath = destinationDir.toAbsolutePath().toString();
        String destFilePath = destFile.toAbsolutePath().toString();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
