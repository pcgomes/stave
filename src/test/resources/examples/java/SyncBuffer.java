class Producer extends Thread {
    SyncBuffer buffer;

    Producer(SyncBuffer b) {
        buffer = b;
    }

    public void run() {
        /** @begin_SyncBlock
         * @condlock buffer->buffer_lock
         * @resource buffer.els->buffer_els
         */
        synchronized (buffer) {
            while (buffer.full()) {
                try {
                    buffer.wait();
                } catch (InterruptedException e) {
                }
            }
            buffer.add();
            buffer.notify();
        }
        /**  @end_SyncBlock */
    }
}

class Consumer extends Thread {
    SyncBuffer buffer;

    Consumer(SyncBuffer b) {
        buffer = b;
    }

    public void run() {
        /** @SyncBlock
         @condvar buffer->buffer_lock
         @resource buffer.els->buffer_els
         */
        synchronized (buffer) {
            while (buffer.empty()) {
                try {
                    buffer.wait();
                } catch (InterruptedException e) {
                }
            }
            buffer.remove();
            buffer.notify();
        }
        //@end_SyncBlock
    }
}

public class SyncBuffer {
    //@begin_Resource
    public int els;
    //@capacity
    public int capacity;

    public static void main(String[] s) {
        //@begin_SyncTask
        //@id SyncBuffer
        //@condlock b->buffer_lock
        //@resouce b.els -> buffer_els
        SyncBuffer b = new SyncBuffer();
        b.els = 1;
        b.capacity = 1;
        //@thread c1->consumer1
        Consumer c1 = new Consumer(b);
        //@thread c2->consumer2
        Consumer c2 = new Consumer(b);
        //@thread c1->producer1
        Producer p1 = new Producer(b);
        c1.start();
        p1.start();
        c2.start();
        //@nnd_SyncTask
    }

    void add() {
        if (els < capacity) {
            els++;
        }
    }

    void remove() {
        if (els > 0) {
            els--;
        }
    }

    boolean full() {
        return els == capacity;
    }
    //@end_Resource

    boolean empty() {
        return els == 0;
    }
}

