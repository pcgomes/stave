/*
* Features nested locking. May deadlock
*/

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
}

class T1 extends Thread {

   public void run() {
   try {
      /* @syncblock _1
      @monitor NestedDead.mlock -> mlock
      @monitor NestedDead.mq    -> mq
      @resource NestedDead.b : Buffer
      */
      synchronized(NestedDead.mlock) {
       synchronized(NestedDead.mq) {
          NestedDead.mq.wait();
       }
      }


   } catch (Exception e) {}

   }
}

class T2 extends Thread {
   public void run() {
      try {
	 /* @syncblock _1
	    @monitor NestedDead.mlock -> mlock
	    @monitor NestedDead.mq    -> mq
	    @resource NestedDead.b : Buffer
	  */
	 synchronized(NestedDead.mlock) {
	    synchronized(NestedDead.mq) {
	       NestedDead.b.add();
	       NestedDead.mq.notify();
	    }
	 }
      } catch (Exception e) {}

   }
}

public class NestedDead {

   public static Object mlock = new Object();
   public static Object mq    = new Object();
   public static Buffer b     = new Buffer();

   /*
   @synctask NestedDead
   @monitor  NestedDead.mlock -> mlock
   @monitor  NestedDead.mq    -> mq
   @resource b -> buf_els
   */
   public static void main( String[] argv) {
      //b = new Buffer();
      b.els = 1;
      b.capacity = 1;
      //@thread
      T1 myt1 = new T1();
      //@thread
      T2 myt2 = new T2();

      myt1.start();
      myt2.start();
   }
}
