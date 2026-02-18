package com.github.fabianpaus.localjavatesting.core;

public class ProcessResult {
    public enum State {
        UNKNOWN,
        EXIT,
        WAIT_INTERRUPTED,
        STREAM_CLOSED,
        EXCEPTION,
    }

    public State state = State.UNKNOWN;
    public int exitValue = 0;
    public Exception exception = null;

    public static ProcessResult exit(int value) {
        ProcessResult result = new ProcessResult();
        result.state = State.EXIT;
        result.exitValue = value;
        return result;
    }

    public static ProcessResult waitInterrupted(Exception ex) {
        ProcessResult result = new ProcessResult();
        result.state = State.WAIT_INTERRUPTED;
        result.exception = ex;
        return result;
    }

    public static ProcessResult streamClosed(Exception ex) {
        ProcessResult result = new ProcessResult();
        result.state = State.STREAM_CLOSED;
        result.exception = ex;
        return result;
    }

    public static ProcessResult exception(Exception ex) {
        ProcessResult result = new ProcessResult();
        result.state = State.EXCEPTION;
        result.exception = ex;
        return result;
    }
}
