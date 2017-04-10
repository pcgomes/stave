Thread LogClient {
   synchronized (lock)
   {
      if ((socket==0))
         wait(socket_open);
      else
         skip;
   }
}
Thread LogServer {
   synchronized (lock)
   {
      socket=1;
      notifyAll(socket_open);
   }
}
main
{
   Lock lock();
   Cond socket_open(lock);
   Int socket(0,1,0); 
   start(1,LogClient);
   start(1,LogServer);
}
