Thread A {
   synchronized (mylock) {
      while (true)  {
         twaiting = twaiting + 1;
         notify( mycond );
         wait( mycond );
      }
   }
}

main {
   Lock mylock();
   Cond mycond(mylock);
   Int twaiting(0,10,0);
   start(2,A);
}
