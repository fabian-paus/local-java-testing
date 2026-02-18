package com.github.fabianpaus.localjavatesting.keycloak;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class KeycloakConfig {
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";

    // Home directory of Keycloak (contains bin/kc.sh, lib/quarkus-run.jar, etc.)
    public Path home;
    public int port = 8080;
    public int managementPort = 9000;
    public final Map<String, String> env = new HashMap<>(Map.of(
            "KC_BOOTSTRAP_ADMIN_USERNAME", DEFAULT_ADMIN_USERNAME,
            "KC_BOOTSTRAP_ADMIN_PASSWORD", DEFAULT_ADMIN_PASSWORD
    ));
    public File importFile;
    public boolean copyToStdout = true;
    public boolean openDebugPort = false;
    public boolean debugSuspend;

    public static KeycloakConfig home(Path homeDirectory) {
        KeycloakConfig config = new KeycloakConfig();
        config.home = homeDirectory;
        return config;
    }

    public KeycloakConfig admin(String username, String password) {
        this.env.put("KC_BOOTSTRAP_ADMIN_USERNAME", username);
        this.env.put("KC_BOOTSTRAP_ADMIN_PASSWORD", password);
        return this;
    }

    /**
     * Use random ports to avoid conflicts with a locally running Keycloak.
     *
     * @return this for chaining operations
     */
    public KeycloakConfig randomPorts() {
        Random random = new Random();
        this.port = 20_000 + random.nextInt(999);
        this.managementPort = this.port + 100;
        return this;
    }

    /**
     * If a debugger is running, we open the Keycloak debug port
     *
     * @return this for chaining operations
     */
    public KeycloakConfig detectDebugger() {
        this.openDebugPort = java.lang.management.ManagementFactory.getRuntimeMXBean().
                getInputArguments().toString().contains("-agentlib:jdwp");
        if (this.openDebugPort) {
            this.copyToStdout = true;
        }
        return this;
    }

    public List<String> makeCommandLine(boolean built) {
        List<String> command = new ArrayList<>();

        command.add("java");

        // Java parameters
        if (openDebugPort) {
            String suspend = debugSuspend ? "y" : "n";
            command.add("-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=" + suspend);
        }
        if (built) {
            command.add("-Dkc.config.built=true");
        }
        command.add("-Dkc.home.dir=" + home);
        command.add("-Djboss.server.config.dir=" + home.resolve("conf"));
        command.add("-Dkeycloak.theme.dir=" + home.resolve("themes"));
        command.add("-Djava.util.logging.manager=org.jboss.logmanager.LogManager");
        command.add("-cp");
        command.add(home.resolve("lib/quarkus-run.jar").toString());
        command.add("io.quarkus.bootstrap.runner.QuarkusEntryPoint");

        // Quarkus parameters
        command.add("--profile=dev");
        command.add("start-dev");

        // Keycloak parameters
        command.add("--http-port");
        command.add(String.valueOf(port));

        command.add("--http-management-port");
        command.add(String.valueOf(managementPort));

        if (importFile != null) {
            String filename = importFile.getName();
            Path importDir = home.resolve("data/import");
            try {
                Files.createDirectories(importDir);
                Path target = importDir.resolve(filename);
                Files.copy(importFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                command.add("--import-realm");
                command.add(filename);
            } catch (IOException e) {
                System.err.println("Could not copy realm import file " + importFile);
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return command;
    }
}
