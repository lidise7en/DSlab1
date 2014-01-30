import java.util.HashMap;


public class TimeStamp {
	
	private int lamportClock;
	private HashMap<String, Integer> vectorClock = null;
	
	public TimeStamp() {
		this.lamportClock = 0;
		this.vectorClock = new HashMap<String, Integer>();
	}

	public int getLamportClock() {
		return lamportClock;
	}

	public void setLamportClock(int lamportClock) {
		this.lamportClock = lamportClock;
	}

	public HashMap<String, Integer> getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(HashMap<String, Integer> vectorClock) {
		this.vectorClock = vectorClock;
	}
	
	/* TODO: Why Vector is a hashmap? */
	public TimeStampRelation compare(TimeStamp ts) {
		return TimeStampRelation.lessEqual;
	}
	
	@Override
	public String toString() {
		return "TimeStamp [lamportClock=" + lamportClock + ", vectorClock="
				+ vectorClock + "]";
	}
}