package custompool;

public class Worker implements Runnable {
    private final TaskQueue queue;
    private final CustomThreadPoolExecutor pool;
    private final long keepAliveMillis;
    private final Thread thread;

    public Worker(CustomThreadPoolExecutor pool, TaskQueue queue, long keepAliveMillis, Thread thread) {
        this.pool = pool;
        this.queue = queue;
        this.keepAliveMillis = keepAliveMillis;
        this.thread = thread;
    }

    @Override
    public void run() {
        try {
            while (!pool.isShutdown()) {
                Runnable task = queue.poll(keepAliveMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (task != null) {
                    System.out.println("[Worker] " + thread.getName() + " executes " + task);
                    task.run();
                } else {
                    System.out.println("[Worker] " + thread.getName() + " idle timeout, stopping.");
                    break;
                }
            }
        } catch (InterruptedException e) {
            // Thread interrupted, exit
        } finally {
            System.out.println("[Worker] " + thread.getName() + " terminated.");
            pool.workerFinished(this);
        }
    }
}
