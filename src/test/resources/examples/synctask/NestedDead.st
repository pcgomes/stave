Thread T2 {
   synchronized (mlock_lock)
   {
      {
         {
            if ((buf_els<max(buf_els))) 
               buf_els=(buf_els+1); 
            else 
               skip;
         }
         notify(mq_cond);
      }
   }
}
Thread T1 {
   synchronized (mlock_lock)
   {
      {
         wait(mq_cond);
      }
   }
}
main 
{
   Lock mq_lock();
   Cond mq_cond(mq_lock);
   Lock mlock_lock();
   Cond mlock_cond(mlock_lock);
   start(1,T2);
   start(1,T1);
}
