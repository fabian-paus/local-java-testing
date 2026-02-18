package com.github.fabianpaus.localjavatesting.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ManagedProcess {

    private Process process;
    public boolean killOnShutdown = true;

    public void onKillStart() {
    }

    public void onKillEnd() {
    }

    public Process startProcess(List<String> command, Map<String, String> env) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.environment().putAll(env);

        builder.redirectErrorStream(true);

        if (killOnShutdown) {
            // Make sure that the process is killed when the main process exits
            Runtime.getRuntime().addShutdownHook(new Thread(this::kill));
        }

        try {
            process = builder.start();

            return process;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void kill() {
        if (process == null) {
            return;
        }

        onKillStart();
        try {
            process.destroy();
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onKillEnd();

        process = null;
    }
}
