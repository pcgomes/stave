Thread MyT {
   synchronized(mylock) {
      while ( waiting < 10  && waiting != 10)  {
         waiting = waiting + 1;
         wait( mycond );
      }
      notify(mycond);
   }
}

main {
   Lock mylock();
   Cond mycond(mylock);
   Int waiting(0,10,0);
   start(10,MyT);
}
