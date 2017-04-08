Thread aFirst {
   synchronized (aLock) {
      notify(aCond);
      wait(aCond);
      synchronized (bLock) {
         notify(aCond);
         wait(bCond);
      }
   }
}

Thread bFirst {
   synchronized (bLock) {
      notify(bCond);
      synchronized (aLock) {
         notify(aCond);
      }
   }
}

main {
   Lock aLock();
   Lock bLock();
   Cond aCond(aLock);
   Cond bCond(bLock);
   start(1,aFirst);
   start(1,bFirst);
}
