package custompool;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CustomExecutor executor = new CustomThreadPoolExecutor(
                2,                      // corePoolSize
                4,                      // maxPoolSize
                5, java.util.concurrent.TimeUnit.SECONDS,   // keepAliveTime
                5,                      // queueSize
                1,                      // minSpareThreads
                new CustomThreadFactory("MyPool"),
                new LoggingRejectionHandler()
        );

        for (int i = 0; i < 10; i++) {
            int taskId = i;
            executor.execute(() -> {
                System.out.println("[Task " + taskId + "] Started");
                try {
                    Thread.sleep(2000); // имитируем работу
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task " + taskId + "] Finished");
            });
        }

        // Ждём немного и затем завершение
        Thread.sleep(15000);
        executor.shutdown();
    }
}
