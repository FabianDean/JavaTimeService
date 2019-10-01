# JavaTimeService
A basic Java server using Sockets to get the time at "time.nist.gov"

## Installation
### Run locally
1) Compile both java files using
```
javac TimeClient.java Server.java
```
2) Run server with
```
java Server
```
3) Go to 'localhost:5000/time' to see results. Other routes found inside Server's giveTime() method
4) Close server when done with ^C in the shell
