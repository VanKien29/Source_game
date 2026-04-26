package server.admin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RuntimeCommandExecutor {

    private static RuntimeCommandExecutor instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Admin Runtime Command");
        thread.setDaemon(true);
        return thread;
    });

    public static synchronized RuntimeCommandExecutor gI() {
        if (instance == null) {
            instance = new RuntimeCommandExecutor();
        }
        return instance;
    }

    public AdminResponse run(String commandName, int timeoutMillis, Callable<AdminResponse> command) {
        Future<AdminResponse> future = executor.submit(command);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return AdminResponse.fail(504, "COMMAND_TIMEOUT", "Lenh runtime qua thoi gian cho phep: " + commandName);
        } catch (Exception e) {
            e.printStackTrace();
            return AdminResponse.fail(500, "COMMAND_FAILED", "Lenh runtime that bai: " + commandName);
        }
    }
}
