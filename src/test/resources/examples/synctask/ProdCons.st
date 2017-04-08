Thread Producer {
   synchronized (mon_lock)
   {
      while ((buf_els==max(buf_els))) 
         wait(mon_cond);
      {
         if ((buf_els<max(buf_els))) 
            buf_els=(buf_els+1); 
         else 
            skip;
      }
      notifyAll(mon_cond);
   }
}
Thread Consumer {
   synchronized (mon_lock)
   {
      while ((buf_els==0)) 
         wait(mon_cond);
      {
         if ((buf_els>0)) 
            buf_els=(buf_els-1); 
         else 
            skip;
      }
      notifyAll(mon_cond);
   }
}
main 
{
   Lock mon_lock();
   Cond mon_cond(mon_lock);
   Int buf_els(0,1,1);
   start(1,Producer);
   start(2,Consumer);
}
