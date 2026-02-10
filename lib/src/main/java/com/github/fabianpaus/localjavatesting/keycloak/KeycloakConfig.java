package com.github.fabianpaus.localjavatesting.keycloak;

import java.io.File;
import java.nio.file.Path;
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
}
