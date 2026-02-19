package com.github.fabianpaus.localjavatesting.core;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.function.Consumer;

public class HealthCheck {
    public int maxTries = 180;
    public Consumer<Long> callback = (waitedMs) -> {};

    public void waitForHttpConnection(String url) {
        for (int i = 0; i < maxTries; ++i) {
            if (testHttpConnection(url)) {
                return;
            }
            try {
                Thread.sleep(1000);
                callback.accept((long) i * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("Failed to establish connection to " + url);
    }

    private static boolean testHttpConnection(String url) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setReadTimeout(1000);
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception ex) {
            return false;
        }
    }
}
