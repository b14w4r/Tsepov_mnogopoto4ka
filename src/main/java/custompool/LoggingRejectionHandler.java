package custompool;

public class LoggingRejectionHandler implements RejectionHandler {
    @Override
    public void rejected(Runnable task) {
        System.out.println("[Rejected] Task " + task + " was rejected due to overload!");
    }
}
