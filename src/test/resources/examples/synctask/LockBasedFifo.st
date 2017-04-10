Thread Producer {
   synchronized (lock)
   {
      while (items==max(items)) wait(notFull);
      items = items+1;
      notify(notEmpty);
   }
}
Thread Consumer {
   synchronized (lock)
   {
      while (items==0) wait(notEmpty);
      items = items-1;
      notify(notFull);
   }
}
main
{
   Lock lock();
   Cond notEmpty(lock);
   Cond notFull(lock);
   Int items(0,1,0);
   start(2,Producer);
   start(1,Consumer);
}
