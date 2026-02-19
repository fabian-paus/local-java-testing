package com.github.fabianpaus.localjavatesting;


import com.github.fabianpaus.localjavatesting.core.HealthCheck;
import com.github.fabianpaus.localjavatesting.keycloak.KeycloakConfig;
import com.github.fabianpaus.localjavatesting.keycloak.KeycloakDownloader;
import com.github.fabianpaus.localjavatesting.keycloak.KeycloakProcess;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        Path home = KeycloakDownloader.download("26.4.7", Paths.get("downloads"));
        System.out.println("Downloaded Keycloak to " + home);
        KeycloakConfig config = KeycloakConfig.home(home)
                .randomPorts()
                .detectDebugger();

        KeycloakProcess process = new KeycloakProcess(config);
        process.start();

        HealthCheck healthCheck = new HealthCheck();
        healthCheck.callback = (waitedMs) -> {
            long seconds = waitedMs / 1000;
            if (seconds % 10 == 0) {
                System.out.println("Waiting " + seconds +"s");
            }
        };
        healthCheck.waitForHttpConnection(
                "http://localhost:" + config.port
        );

        System.out.println("Keycloak server started");

        process.kill();
    }
}