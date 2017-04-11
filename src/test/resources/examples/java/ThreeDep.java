/*
@resource 
@object myval -> myval
@value myval -> myval
@capacity capacity
*/
class MyBool {
    // @defaultval
    int myval = 0;
    // @defaultcap
    int capacity = 1;

    //@operation @inline
    void setTrue() {
        myval = 1;
    }

    //@operation @inline
    void setFalse() {
        myval = 0;
    }

    /* @predicate @inline */
    boolean isTrue() {
        return myval == 1;
    }

    /* @predicate @inline */
    boolean isFalse() {
        return myval == 0;
    }

}

class A extends ThreeDep {

    public void run() {
        try {
  /* @syncblock _1
  @monitor mon-> mon
  @resource x : MyBool
  @resource y : MyBool
  */
            synchronized (mon) {
                while (!(x.isTrue() && y.isTrue())) {
                    mon.wait();
                }
            }
            x.setFalse();
            mon.notifyAll();
        } catch (Exception e) {
        }
    }
}

class B extends ThreeDep {

    public void run() {
        try {
  /* @syncblock _1
  @monitor mon-> mon
  @resource x : MyBool
  @resource y : MyBool
  */
            synchronized (mon) {
                while (!(x.isTrue() || !y.isTrue())) {
                    mon.wait();
                }
            }
            x.setTrue();
            mon.notifyAll();
        } catch (Exception e) {
        }
    }
}

class C extends ThreeDep {
    public void run() {
        try {
  /* @syncblock _1
  @monitor mon-> mon
  @resource x : MyBool 
  @resource y : MyBool 
  */
            synchronized (mon) {
                while (!(x.isTrue())) {
                    mon.wait();
                }
            }
            y.setTrue();
            mon.notifyAll();
        } catch (Exception e) {
        }
    }
}

public class ThreeDep extends Thread {
    static MyBool x;
    static MyBool y;
    static Object mon = new Object();

    /*
    @synctask ThreeDep
    @monitor  mon -> mon
    @resource x -> x
    @resource y -> y
    */
    public static void main(String[] argv) {
        //@thread
        A mya = new A();
        //@thread
        B myb = new B();
        //@thread
        C myc = new C();

        mya.start();
        myb.start();
        myc.start();

    }
}
