package com.github.fabianpaus.localjavatesting.keycloak;

import com.github.fabianpaus.localjavatesting.core.ManagedProcess;
import com.github.fabianpaus.localjavatesting.core.ProcessResult;

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

    @Override
    public boolean onExit(ProcessResult result) {
        if (result.state == ProcessResult.State.EXIT) {
            System.out.println("Keycloak: Process exited with code " + result.exitValue);
            // Keycloak returns exit code 10 if the Quarkus build has been triggered.
            // In this case, we need to restart the same process again.
            if (result.exitValue == 10) {
                startKeycloakProcess(true);
                return true;
            }
        } else {
            System.out.println("Keycloak: Process aborted with exception " + result.exception.getMessage());
        }
        return false;
    }

    public void start() {
        startKeycloakProcess(false);
    }

    private void startKeycloakProcess(boolean built) {
        List<String> command = config.makeCommandLine(built);
        startProcess(command, config.env);
    }
}
