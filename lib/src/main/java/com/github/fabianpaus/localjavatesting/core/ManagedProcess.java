package com.github.fabianpaus.localjavatesting.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagedProcess {

    private Process process;
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public ManagedProcessConfig config = new ManagedProcessConfig();

    public void onKillStart() {
    }

    public void onKillEnd() {
    }

    public boolean onExit(ProcessResult result) {
        return false;
    }

    public void startProcess(List<String> command, Map<String, String> env) {
        if (config.killOnShutdown) {
            // Make sure that the process is killed when the main process exits
            Runtime.getRuntime().addShutdownHook(new Thread(this::kill));
        }

        restartProcess(command, env);
        executor.submit(this::runProcess);
    }

    public void restartProcess(List<String> command, Map<String, String> env) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.environment().putAll(env);

        builder.redirectErrorStream(true);

        System.out.println(String.join(" ", command.stream().map((arg) -> "\"" + arg + "\"").toList()));

        try {
            process = builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void kill() {
        if (process == null) {
            return;
        }

        onKillStart();
        executor.shutdown();
        try {
            process.destroy();
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onKillEnd();

        process = null;
    }

    private void runProcess() {
        boolean restart;
        do {
            ProcessResult result = readOutput();
            restart = onExit(result);
        } while (restart);
    }

    private ProcessResult readOutput() {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            // Read until the stream is exhausted, meaning the process has terminated
            while ((line = reader.readLine()) != null) {
                logs.add(line);
                if (config.copyToStdOut) {
                    System.out.println(line);
                }
            }
            try {
                int value = process.waitFor();
                return ProcessResult.exit(value);
            } catch (InterruptedException ex) {
                return ProcessResult.waitInterrupted(ex);
            }
        } catch (IOException ex) {
            if (ex.getMessage().startsWith("Stream closed")) {
                return ProcessResult.streamClosed(ex);
            } else {
                return ProcessResult.exception(ex);
            }
        }
    }
}
