Thread B {
   synchronized (lock)
   {
      while(false) {
         wait(cond);
      }
   }
}
Thread R {
   synchronized (lock)
   {
      notify(cond);
   }
}
Thread Y {
   synchronized (lock)
   {
      if(true) {
      notifyAll(cond);
      } else skip;
   }
}
main 
{
   Lock lock();
   Cond cond(lock);
   start(1,B);
   start(1,R);
   start(1,Y);
}
