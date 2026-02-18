package com.github.fabianpaus.localjavatesting;


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

        process.kill();
    }
}