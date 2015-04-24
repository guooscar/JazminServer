/**
 * 
 */
package jazmin.server.sip.io.pkts.framer;

import java.util.concurrent.atomic.AtomicLong;

import jazmin.server.sip.io.pkts.Clock;
import jazmin.server.sip.io.pkts.Pcap;

/**
 * FramerFactory
 * 
 * @author jonas@jonasborjesson.com
 */
public final class FramerManager {

    private static final FramerManager instance = new FramerManager();

    /**
     * The current time in the system, which is driven by
     * {@link Pcap#loop(io.pkts.FrameHandler)}.
     */
    private final PcapClock clock = new PcapClock();

    public static final FramerManager getInstance() {
        return instance;
    }

    /**
     * 
     */
    private FramerManager() {
        // left empty intentionally
    }

    /**
     * Move the {@link Clock} to the specified time.
     * 
     * @param time
     */
    public void tick(final long time) {
        this.clock.tick(time);
    }

    private static class PcapClock implements Clock {

        private final AtomicLong currentTime = new AtomicLong();

        public PcapClock() {
            // left empty intentionally
        }

        @Override
        public long currentTimeMillis() {
            return this.currentTime.get();
        }

        public void tick(final long time) {
            // final SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
            // final Date date = new Date(time / 1000);
            // System.out.println("Time is: " + formatter.format(date));
            this.currentTime.set(time);
        }

    }

}
