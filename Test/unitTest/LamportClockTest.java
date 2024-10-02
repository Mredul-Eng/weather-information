import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LamportClockTest {
    private LamportClock lamportClock;

    @Before
    public void setUp() throws Exception {
        lamportClock = new LamportClock();
    }

    //verify the initial time
    @Test
    public void testInitialTime() throws Exception {
        assertEquals(0, lamportClock.getTime());
    }

    //verify the time is increased or not by calling the incrementTime() function
    @Test
    public void testIncrementTime() {
        lamportClock.incrementTime();
        assertEquals(1, lamportClock.getTime());
    }

    //verify the updateTime(int receivedTime) function's behavior when the receivedTime is greater than current clock's time.
    @Test
    public void testUpdateTimeWithLargerReceivedTime() throws Exception {
        lamportClock.updateTime(5);
        assertEquals(6, lamportClock.getTime()); // as current clock's time(0 initially) smaller than receivedTime, it updates the time by one.

    }
    @Test
    public void testUpdateTimeWithSmallerReceivedTime() throws Exception {
        lamportClock.updateTime(3);// as current clock's time(0 initially) smaller than receivedTime, it updates the time by one.
        lamportClock.updateTime(2);// as current clock's time(6) larger than receivedTime, it doesn't update the time.
        assertEquals(4, lamportClock.getTime()); // time should increment when the current clock smaller than receivedTime
    }
    @Test
    public void multipleUpdateTimes() throws Exception {
        lamportClock.incrementTime(); // time will be 1
        lamportClock.updateTime(4); // time will be 5
        lamportClock.incrementTime(); // time will be 6
        lamportClock.updateTime(10); // time will be 11
        assertEquals(11, lamportClock.getTime());
    }

}