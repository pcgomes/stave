Thread Producer {
 synchronized (mylock) {
    wait(mycond);
 }
}

main {
   Lock mylock();
   Cond mycond(mylock);
   start(1,Producer);
}
