package custompool;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public interface CustomExecutor extends Executor {
    @Override
    void execute(Runnable command);
    <T> Future<T> submit(Callable<T> callable);
    void shutdown();
    void shutdownNow();
}
