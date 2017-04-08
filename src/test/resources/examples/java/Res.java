import java.util.LinkedList;

class Prod extends Thread {
   Res buffer;
   Prod(Res b) {
      buffer = b;
   }
   public void run() {
	/*
	* @syncblock _1
	* @threadtype Prod
	* @lock buffer -> mon_lock
        * @condvar buffer -> mon_cond
	* @resource buffer : Res 
	*/
      synchronized(buffer) {
	 while (buffer.full()) {
	    try {
	       buffer.wait();
	    } catch (InterruptedException | NullPointerException e) {}
	 }
	 buffer.add(this);
	 buffer.notifyAll();
      }
   }
}

class Cons extends Thread {
   Res buffer;
   Cons(Res b) {
      buffer = b;
   }

   public void run() {
      /* @syncblock _1 @monitor buffer-> mon @resource buffer : Res @threadtype Cons */
      synchronized(buffer)   {
	 while (buffer.empty()) 
	    try {
	       buffer.wait();
	    } catch (InterruptedException e) {}
	 buffer.remove();
	 buffer.notifyAll();
      }
   }  
}

/*
@resource 
@object mybuf -> buf_els
@value mybuf.size() -> buf_els
@capacity maxsize
*/
public class Res {

   /* @defaultval 0 */
   protected LinkedList<Object> mybuf = new LinkedList<Object>();

   // @defaultcap
   protected int maxsize = 3;

   Res() {
   }

   Res(int lcap) {
      maxsize = lcap;
   }

   /* @operation
   *  @map  mybuf.push(myobj) -> @{ buf_els = buf_els + 1; }@
   *  @inline
   */
   public void add(Object myobj) {
      if (mybuf.size() < maxsize) mybuf.push(myobj);
   }

   /*
   * @operation  @code @{ if (buf_els > 0) buf_els=buf_els-1; else {} }@ */
   public void remove() {
      if (mybuf.size() > 0) mybuf.pop();
   }

   /* @predicate
      @inline */
   boolean full() {
      return mybuf.size() == maxsize;
   }
   /*
    * @predicate @code @{ buf_els == 0 }@  */
   boolean empty() {
      return mybuf.size() == 0;
   }

   /*
   @synctask Buffer
   @monitor  b -> mon
   @resource b -> buf_els
   @resource mybool -> mybool
   */
   public static void main(String[] s) {
      // Not a useful comment
      int varone, vartwo, varthree=3;
      boolean mybool = false;
      Res b = new Res(1);
      b.add(b);
      // @thread
      Cons c1 = new Cons(b);
      // @thread 
      Cons c2 = new Cons(b);
      // @thread
      Prod p1 = new Prod(b);
      c1.start();
      p1.start();
      c2.start();
   }

   // This fields should be unfolded. Not a useful comment
   public  int fa, fb=3;
   // Another comment
   private int fc=10;
}

