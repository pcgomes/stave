Thread Producer {
   synchronized (buffer_lock)
   {
      while (buffer_lock) 
      {
      }
      notifyAll(buffer_lock);
   }
}
Thread Consumer {
   synchronized (buffer_lock)
   {
      while (buffer_lock) 
      {
         wait(buffer_lock);
      }
      notifyAll(buffer_lock);
   }
}
main 
{
}
