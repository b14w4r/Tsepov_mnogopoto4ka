package custompool;

public interface RejectionHandler {
    void rejected(Runnable task);
}
