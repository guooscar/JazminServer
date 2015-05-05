package jazmin.server.relay.webrtc;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 
 * @author g2131
 *
 */
public class ByteArrayBlockingQueue {
	final byte[] items;
	final ReentrantLock lock;
	private final Condition newSpace;
	private final Condition newElement;
	int takeIndex;
	int putIndex;
	public int count;
	//
	volatile boolean isClosed=false;

	public ByteArrayBlockingQueue(int capacity, boolean fair) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		this.items = new byte[capacity];
		lock = new ReentrantLock(fair);
		newSpace = lock.newCondition();
		newElement = lock.newCondition();
	}

	public ByteArrayBlockingQueue(int capacity) {
		this(capacity, false);
	}
	
	public void close(){
		isClosed=true;
	}
	/**
	 * Inserts element at current put position, advances, and signals.
	 * Call only when holding lock.
	 */
	private void enqueue(byte x) {
		// assert lock.getHoldCount() == 1;
		// assert items[putIndex] == null;
		final byte[] items = this.items;
		items[putIndex] = x;
		if (++putIndex == items.length)
			putIndex = 0;
		count++;
		newSpace.signal();
	}

	/**
	 * Inserts elements starting at the current put position, advances, and signals
	 * Call only when holding lock.
	 */
	private void enqueue(byte[] x) {
		for (byte b : x)
			enqueue(b);
	}

	/**
	 * Extracts element at current take position, advances, and signals.
	 * Call only when holding lock.
	 */
	private byte dequeue() {
		// assert lock.getHoldCount() == 1;
		// assert items[takeIndex] != null;
		final byte[] items = this.items;
		byte x = items[takeIndex];
		if (++takeIndex == items.length)
			takeIndex = 0;
		count--;
		newElement.signal();
		return x;
	}

	/**
	 * Extracts elements starting at the current take position, advances and signals.
	 * Call only when holding lock.
	 */
	private byte[] dequeue(int x) {
		byte[] ret = new byte[x];
		for (int i = 0; i < ret.length; i++)
			ret[i] = dequeue();
		return ret;
	}

	private byte[] dequeue(byte[] b) {
		for (int i = 0; i < b.length; i++)
			b[i] = dequeue();
		return b;
	}
	
	public boolean offer(byte b) {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count == items.length)
				return false;
			else {
				enqueue(b);
				return true;
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean offer(byte[] b) {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (items.length - count < b.length)
				return false;
			else {
				enqueue(b);
				return true;
			}
		} finally {
			lock.unlock();
		}
	}

	public void put(byte b) throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == items.length)
				newElement.await();
			enqueue(b);
		} finally {
			lock.unlock();
		}
	}

	public void put(byte[] b) throws InterruptedException {
		if(isClosed){
			return;
		}
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (items.length - count < b.length)
				newElement.await();
			enqueue(b);
		} finally {
			lock.unlock();
		}
	}

	public byte poll() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (count == 0) ? null : dequeue();
		} finally {
			lock.unlock();
		}
	}

	public byte[] poll(int i) {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (count < i) ? null : dequeue(i);
		} finally {
			lock.unlock();
		}
	}

	public byte take() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == 0)
				newSpace.await();
			return dequeue();
		} finally {
			lock.unlock();
		}
	}

	public byte[] take(int i) throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count < i)
				newSpace.await();
			return dequeue(i);
		} finally {
			lock.unlock();
		}
	}

	public int take(byte[] b) throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		int readCount=0;
		try {
			while (count < b.length){
				newSpace.await();
			}
			readCount++;
			dequeue(b);
			return readCount;
		} finally {
			lock.unlock();
		}
	}

	public int available() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return count;
		} finally {
			lock.unlock();
		}
	}
}