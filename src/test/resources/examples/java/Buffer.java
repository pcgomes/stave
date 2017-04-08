import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class Producer extends Thread {
  Buffer buffer;
  Producer(Buffer b) {
   buffer = b;
  }
  public void run() {
   /*
   @syncblock 1
   @threadtype Producer
   @condlock buffer->buffer_lock 
   @resource buffer.els->buffer_els
   */
   buffer.lock();
   while (buffer.full()) 
     try {
     buffer.notFull.await();
     } catch (InterruptedException | NullPointerException e) {}
   buffer.add();
   buffer.notEmpty.signal();
   buffer.unlock();
  }
}

class Consumer extends Thread {
 Buffer buffer;
 Consumer(Buffer b) {
  buffer = b;
 }
 public void run() {
  /* @syncblock 1 @condlock buffer-> buffer_lock @resource buffer.els->buffer_els */
  buffer.lock();
   while (buffer.empty()) 
    try {
    buffer.notEmpty.await();
    } catch (InterruptedException e) {}
  buffer.remove();
  buffer.notFull.signal();
  buffer.unlock();
 }  
}

public class Buffer extends ReentrantLock {
 public Condition notEmpty;
 public Condition notFull;
 public static Consumer c1;
 public static Consumer c2;
 public static Producer p1;
 //@begin_Resource 
 public int els;
 //@capacity
 public int capacity;
 void add() {
  if (els < capacity) els++; }
 void remove() {
  if (els > 0) els--;}
 boolean full() {
  return els == capacity; }
 boolean empty()
  { return els == 0; }
 //@end_Resource

 public static void main(String[] s) {
  /*@synctask Buffer
  @condlock b->buffer_lock
  @resouce b.els -> buffer_els
  */
  Buffer b = new Buffer();
  b.notEmpty = b.newCondition();
  b.notFull  = b.newCondition();
  b.els = 1;
  b.capacity = 1;
  // @thread c1->consumer1
  Consumer c1 = new Consumer(b);
  // @thread c2->consumer2
  Consumer c2 = new Consumer(b);
  // @thread c1->producer1
  Producer p1 = new Producer(b);
  c1.start();
  p1.start();
  c2.start();
  //@end_SyncTask
 }
}

