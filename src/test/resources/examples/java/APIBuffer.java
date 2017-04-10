class Producer extends Thread {
   APIBuffer buffer;
   Producer(APIBuffer b) {
      buffer = b;
   }
   public void run() {
	/*
	* @syncblock _1
	* @threadtype Producer
	* @condlock buffer -> buffer_lock 
	* @resource buffer.els->buffer_els
	*/
      synchronized(buffer) {
	 while (buffer.full()) {
	    try {
	       buffer.wait();
	    } catch (InterruptedException | NullPointerException e) {}
	 }
	 buffer.add();
	 buffer.notifyAll();
      }
   }
}

class Consumer extends Thread {
   APIBuffer buffer;
   Consumer(APIBuffer b) {
      buffer = b;
   }

   public void run() {
      /* @syncblock _1 @condlock buffer-> buffer_lock @resource buffer.els->buffer_els @threadtype Consumer */
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
@resource APIBuffer
@var els
@capacity capacity
*/
public class APIBuffer {
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

   /*@synctask Buffer
   @condlock b->buffer_lock
   @resource b->buffer_els
   */
   public static void main(String[] s) {
      APIBuffer b = new APIBuffer();
      b.els = 1;
      b.capacity = 1;
      // @thread c1 -> Consumer
      Consumer c1 = new Consumer(b);
      // @thread c2 -> Consumer2
      Consumer c2 = new Consumer(b);
      // @thread c1 -> Producer1
      Producer p1 = new Producer(b);
      c1.start();
      p1.start();
      c2.start();
   }
}

