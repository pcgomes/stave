Thread Consumer {
 synchronized(mylock) {
    while ( true ) {
       wait(notEmpty);
    }
 }
}

main {
   Lock mylock();
   Cond notEmpty(mylock);
   start(2,Consumer);
}
