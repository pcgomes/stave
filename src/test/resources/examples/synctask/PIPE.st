Thread LogClient {
   synchronized (lock)
   {
      if ((!socket))
         wait(socket_open);
      else
         skip;
   }
}
Thread LogServer {
   synchronized (lock)
   {
      socket=true;
      notifyAll(socket_open);
   }
}
main
{
   Lock lock();
   Cond socket_open(lock);
   Bool socket(false); 
   start(1,LogClient);
   start(1,LogServer);
}
