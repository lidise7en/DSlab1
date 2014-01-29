
public abstract class ClockService {

	private TimeStamp ts;
	public ClockService(TimeStamp newTS) {
		this.ts = newTS;
	}

	public TimeStamp getTs() {
		return ts;
	}
	public void setTs(TimeStamp ts) {
		this.ts = ts;
	}
	
	public abstract void updateTS(TimeStamp ts);
}
