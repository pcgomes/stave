Thread A {
   synchronized (mylock) {
      synchronized (mylock) {
         while (true)  {
            notify( mycond );
            wait( mycond );
        }
      }
   }
}

Thread B {
   synchronized (mylock) {
      while (true)  {
         notify( mycond );
         wait( mycond );
      }
   }
}

main {
   Lock mylock();
   Cond mycond(mylock);
   start(2,A);
   start(2,B);
}
