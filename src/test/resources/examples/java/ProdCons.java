class Producer extends Thread {
  Buffer buffer;
  Producer(Buffer b) { buffer = b; }
  public void run() {
  try{
  /* @syncblock _1
  @monitor buffer-> mon
  @resource buffer : Buffer
  */
   synchronized(buffer){
   while (buffer.full()) 
     buffer.wait();
   buffer.add();
   buffer.notifyAll();
   }
  } catch(Exception e){}
  }
}

class Consumer extends Thread {
 Buffer buffer;
 Consumer(Buffer b) { buffer = b; }
 public void run() {
  try{
  /* @syncblock _1
  @monitor buffer-> mon
  @resource buffer : Buffer
  */
  synchronized(buffer) {
   while (buffer.empty()) 
    buffer.wait();
   buffer.remove();
   buffer.notifyAll();
  }
  } catch(Exception e){}
 }
}  

/*
@resource 
@object els -> buf_els
@value els -> buf_els
@capacity capacity 
*/
class Buffer {
 // @defaultval
 int els=1;
 // @defaultcap
 int capacity=1;
 //@operation @inline
 void remove() { if (els > 0) els = els - 1;}
 //@operation @inline
 void add() { if (els < capacity) els = els + 1; }
 /* @predicate @inline */
 boolean full() { return els == capacity; }
 /* @predicate @inline */
 boolean empty() { return els == 0; }

 /*
 @synctask Buffer
 @monitor  b -> mon
 @resource b -> buf_els
 */
 public static void main(String[] s) {
  Buffer b = new Buffer();
  b.els = 1;
  b.capacity = 1;
  //@thread
  Consumer c1 = new Consumer(b);
  //@thread
  Consumer c2 = new Consumer(b);
  //@thread
  Producer p1 = new Producer(b);
  c1.start();
  p1.start();
  c2.start();
 }
}
