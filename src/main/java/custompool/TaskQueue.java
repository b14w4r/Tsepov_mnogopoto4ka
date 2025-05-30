package custompool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    private final BlockingQueue<Runnable> queue;

    public TaskQueue(int size) {
        this.queue = new LinkedBlockingQueue<>(size);
    }

    public boolean offer(Runnable task) {
        return queue.offer(task);
    }

    public Runnable poll(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
