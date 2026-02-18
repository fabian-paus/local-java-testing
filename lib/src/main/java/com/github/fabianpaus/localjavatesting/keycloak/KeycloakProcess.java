package com.github.fabianpaus.localjavatesting.keycloak;

import com.github.fabianpaus.localjavatesting.core.ManagedProcess;

import java.util.List;

public class KeycloakProcess extends ManagedProcess {
    public KeycloakConfig config;

    public KeycloakProcess(KeycloakConfig config) {
        this.config = config;
    }

    public static KeycloakProcess start(KeycloakConfig config) {
        KeycloakProcess process = new KeycloakProcess(config);
        process.start();
        return process;
    }

    public void start() {

    }

    private Process startKeycloakProcess(boolean built) {
        List<String> command = config.makeCommandLine(built);
        return startProcess(command, config.env);
    }
}
