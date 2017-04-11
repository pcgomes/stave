# STaVe - The SyncTask Verifier

STaVe is a tool to verify the correct usage of condition variables.
It takes as input annotated Java code and extracts the expected synchronization scheme as a SyncTask program.
SyncTask is a non-procedural Java-like imperative language to define synchronization schemes using condition.
STaVe translates SyncTask into Coloured Petri nets or Promela models,
which can be verified by CPN Tools and Spin, respectively, to prove/disprove the synchronization correctness.

## Build

Execute the following command to compile STaVe, download the dependencies to target/lib, and pack it as a JAR.

    mvn package

## Run

The follwing commands are examples executed from the top-level local repository directory,
and assume that STaVe was succesfully built and described above.

List the command syntax and options:

    java -cp target/lib/\*:target/stave-1.0-SNAPSHOT.jar stave.Main --help

Alternatively:

    export CLASSPATH="target/lib/\*:target/stave-1.0-SNAPSHOT.jar";
    java stave.Main --help

Process an annotated Java program as input, and output a SyncTask program:

    java -cp target/lib/\*:target/stave-1.0-SNAPSHOT.jar stave.Main -os Example.st -ij Example.java

Process a SyncTask program, and output a Coloured Petri net:

    java -cp target/lib/\*:target/stave-1.0-SNAPSHOT.jar stave.Main -oc Example.cpn -is Example.st

## More info

Visit http://www.csc.kth.se/~pedrodcg/stave/
