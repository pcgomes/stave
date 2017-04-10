Thread Producer {
 synchronized (mylock) {
   wait(notFull);
 }
}

Thread Consumer {
 synchronized(mylock) {
   wait(notEmpty);
 }
}
main {
   Lock mylock();
   Cond notEmpty(mylock);
   Cond notFull(mylock);
   start(1,Producer);
   start(2,Consumer);
}
