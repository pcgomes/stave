class Producer extends Thread {
 Buffer buffer1;
 Buffer buffer2;
 Producer(Buffer pb){buffer1=pb;buffer2=pb;}
 Producer(Buffer pb1, Buffer pb2){buffer1=pb1;buffer2=pb2;}
 public void run() {
  /*@syncblock
   *  @monitor buffer1 -> m
   *  @resource buffer:Buffer->b_els1
   *  @resource buffer:Buffer->b_els2 */
  synchronized(buffer1) {
   try {
    while (buffer1.full() && !buffer2.empty())
     buffer1.wait();
    buffer1.add();
    buffer2.remove();
    buffer1.notifyAll();
   } catch(Exception e) {}
  }
 }
}

class Consumer extends Thread {
 Buffer buf1;
 Buffer buf2;
 Consumer(Buffer pb){buf1=pb;buf2=pb;}
 Consumer(Buffer pb1, Buffer pb2){buf1=pb1;buf2=pb2;}
 public void run() {
  /*@syncblock
   * @monitor buf1 -> m
   * @resource buf1:Buffer->b_els1 
   * @resource buf2:Buffer->b_els2 */
  synchronized(buf1) {
   try { 
   while (buf1.empty() && ! buf1.full())
    buf1.wait();
    buf1.remove();
    buf2.add();
    buf1.notifyAll();
   } catch(Exception e) {}
  }
 }
}

/*@resource Buffer
 *  @capacity cap
 *  @object els -> els
 *  @value els -> els */
abstract class AbstractBuffer {
 protected int els; int cap;
 /* @predicate @inline */
 boolean full(){return els==cap;}
 /* @predicate @inline */
 boolean empty(){return els==0;}
}

/*@resource Buffer */
class Buffer extends AbstractBuffer {
 /* @operation @inline */
 void remove(){if (els>0)els--;}
 /* @operation @inline */
 void add(){if (els<cap)els++;}

 /*@synctask Buffer
  *  @monitor ba -> m
  *  @resource ba -> b_els1
  *  @resource bb -> b_els2 */
 static void St1() {
  Buffer ba = new Buffer();
  Buffer bb = new Buffer();
  ba.els = 1; ba.cap = 7;
  bb.els = 3; bb.cap = 17;
  /* @thread */
  Consumer c1 = new Consumer(ba,bb);
  /* @thread */
  Consumer c2 = new Consumer(ba,bb);
  /* @thread */
  Producer p = new Producer(ba,bb);
  c1.start();
  p.start();
  c2.start();
 }

 /*@synctask Buffer
  *  @monitor b1 -> m
  *  @resource b1 -> b_els1 
  *  @resource b2 -> b_els2 */
 static void St2() {
  Buffer b1 = new Buffer();
  Buffer b2 = new Buffer();
  b1.els = 1; b1.cap = 7;
  b2.els = 2; b2.cap = 7;
  /* @thread */
  Consumer c1 = new Consumer(b1,b2);
  /* @thread */
  Consumer c2 = new Consumer(b1,b2);
  /* @thread */
  Producer p = new Producer(b1,b2);
  c1.start();
  p.start();
  c2.start();
 }
 public static void main(String[] argv) {
	 St1();
	 St2();
 }
}

