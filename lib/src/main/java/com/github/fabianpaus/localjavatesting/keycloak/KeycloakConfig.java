package com.github.fabianpaus.localjavatesting.keycloak;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class KeycloakConfig {
    // Home directory of Keycloak (contains bin/kc.sh, lib/quarkus-run.jar, etc.)
    public Path home;
    public int port;
    public int managementPort;
    public final Map<String, String> env = new HashMap<>();
    public File importFile;
    public boolean copyToStdout;
    public boolean openDebugPort;
    public boolean debugSuspend;


    public static KeycloakConfig downloadAndSetup(String version, Path parentDirectory) {
        KeycloakConfig config = new KeycloakConfig();



        return config;
    }
}
