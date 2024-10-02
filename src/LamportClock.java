// This class will help to implement a logical clock using Lamport clock algorithm
// that helps to ordering events.
public class LamportClock {
    private int time; //hold current time

    public LamportClock() {
        this.time = 0; // the clock will start from 0.

    }
    //Before executing any events(such as sending or receiving message),
    // the process will increment the clock's time by 1. The method should be synchronized for handling multiple client.
    public synchronized void incrementTime() {
        time++;
    }

    //This method update the local Lamport clock's time based on the received timeStamp(the received clock time)
    // from another system or process.The method should be synchronized for handling multiple client.
    public synchronized void updateTime(int receivedTime) {
        if(time > receivedTime) {
            return;
        }
        else{
            time = Math.max(time, receivedTime) + 1; // Take maximum time between the current time and received time,
            // then increment the time by 1. Mainly this method update the time for receiving process.
            // The method should be synchronized for handling multiple client.
        }

    }

   //This method return the current time.The method should be synchronized for handling multiple client.
    public synchronized int getTime() {
        return time;
    }
}
