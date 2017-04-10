Thread Prod {
   synchronized (mon_lock)
   {
      while ((buf_els==max(buf_els))) 
      {
         {
            wait(mon_cond);
         }
      }
      {
         if ((buf_els<max(buf_els))) 
            buf_els=(buf_els+1); 
         else 
            skip;
      }
      notifyAll(mon_cond);
   }
}
Thread Cons {
   synchronized (mon_lock)
   {
      while ((buf_els==0)) 
      {
         wait(mon_cond);
      }
      if ((buf_els>0)) 
         buf_els=(buf_els-1); 
      else 
      {
      }
      notifyAll(mon_cond);
   }
}
main 
{
   Lock mon_lock();
   Cond mon_cond(mon_lock);
   Bool mybool(false);
   Int buf_els(0,3,0);
   start(1,Prod);
   start(2,Cons);
}
