Thread Consumer {
 synchronized(mylock) {
    while ( (mya) && (myb < 8) ) {
       if ((mya) && (mya)) {
       myb = 10;
       } else { skip; }
    }
 }
}

main {
   Bool mya(true);
   Int  myb(0,10,3);
   Lock mylock();
   start(1,Consumer);
}
