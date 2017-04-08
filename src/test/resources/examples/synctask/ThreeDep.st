Thread A {
   synchronized (mon_lock)
   {
      while ((!((myval==1)&&(myval==1)))) 
      {
         wait(mon_cond);
      }
   }
}
Thread C {
   synchronized (mon_lock)
   {
      while ((!(myval==1))) 
      {
         wait(mon_cond);
      }
   }
}
Thread B {
   synchronized (mon_lock)
   {
      while ((!((myval==1)||(!(myval==1))))) 
      {
         wait(mon_cond);
      }
   }
}
main 
{
   Lock mon_lock();
   Cond mon_cond(mon_lock);
   start(1,A);
   start(1,C);
   start(1,B);
}
