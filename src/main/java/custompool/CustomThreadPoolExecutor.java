package custompool;

  import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutor implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveMillis;
    private final int queueSize;
    private final int minSpareThreads;

    private final CustomThreadFactory threadFactory;
    private final RejectionHandler rejectionHandler;

    private final List<TaskQueue> taskQueues = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final Set<Worker> activeWorkers = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    private volatile boolean isShutdown = false;

    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit,
                                    int queueSize, int minSpareThreads,
                                    CustomThreadFactory threadFactory, RejectionHandler rejectionHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveMillis = unit.toMillis(keepAliveTime);
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = threadFactory;
        this.rejectionHandler = rejectionHandler;

        // Инициализация базовых очередей и потоков
        for (int i = 0; i < corePoolSize; i++) {
            createWorker();
        }
    }

    private synchronized void createWorker() {
        if (threads.size() >= maxPoolSize) return;

        TaskQueue queue = new TaskQueue(queueSize);
        taskQueues.add(queue);

        Runnable dummy = () -> {};
        Thread t = threadFactory.newThread(dummy);
        Worker worker = new Worker(this, queue, keepAliveMillis, t);
        Runnable wrapper = () -> worker.run();
        Thread realThread = new Thread(wrapper, t.getName());
        threads.add(realThread);
        activeWorkers.add(worker);
        realThread.start();
    }

    private TaskQueue getNextQueue() {
        int index = roundRobinIndex.getAndIncrement() % taskQueues.size();
        return taskQueues.get(index);
    }

    private synchronized void ensureSpareThreads() {
        int freeThreads = 0;
        for (TaskQueue q : taskQueues) {
            if (q.isEmpty()) freeThreads++;
        }
        int needed = minSpareThreads - freeThreads;
        for (int i = 0; i < needed; i++) {
            createWorker();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            rejectionHandler.rejected(command);
            return;
        }

        TaskQueue queue = getNextQueue();
        if (!queue.offer(command)) {
            rejectionHandler.rejected(command);
        } else {
            System.out.println("[Pool] Task accepted into queue: " + command);
        }

        ensureSpareThreads();
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<>(callable);
        execute(task);
        return task;
    }

    @Override
    public void shutdown() {
        System.out.println("[Pool] Shutdown initiated.");
        isShutdown = true;
    }

    @Override
    public void shutdownNow() {
        System.out.println("[Pool] Immediate shutdown initiated.");
        isShutdown = true;
        for (Thread t : threads) {
            t.interrupt();
        }
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void workerFinished(Worker w) {
        activeWorkers.remove(w);
    }
}
