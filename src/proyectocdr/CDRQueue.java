package proyectocdr;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CDRQueue {
    private final BlockingQueue<CDR> queue;

    public CDRQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put(CDR cdr) throws InterruptedException {
        queue.put(cdr);
    }

    public CDR take() throws InterruptedException {
        return queue.take();
    }
}
