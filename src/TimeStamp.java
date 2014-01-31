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
	
	public TimeStampRelation compare(TimeStamp ts) {
		if(this.vectorClock.size() == 0) {//this is a lamport timestamp
			if(this.lamportClock == ts.lamportClock) {
				return TimeStampRelation.equal;
			}
			else if(this.lamportClock > ts.lamportClock) {
				return TimeStampRelation.greaterEqual;
			}
			else {
				return TimeStampRelation.lessEqual;
			}
		}
		else {//this is a vector timestamp
			int flag = 0;
			for(String e : this.vectorClock.keySet()) {
				if(this.vectorClock.get(e) == ts.getVectorClock().get(e)) {
					continue;
				}
				else if(this.vectorClock.get(e) > ts.getVectorClock().get(e)) {
					if(flag == -1) {
						return TimeStampRelation.concurrent;
					}
					else if(flag == 0){
						flag = 1;
					}
					else {
						continue;
					}
				}
				else {
					if(flag == 1) {
						return TimeStampRelation.concurrent;
					}
					else if(flag == 0) {
						flag = -1;
					}
					else {
						continue;
					}
				}
			}
			if(flag == 0)
				return TimeStampRelation.equal;
			else if(flag == 1) {
				return TimeStampRelation.greaterEqual;
			}
			else {
				return TimeStampRelation.lessEqual;
			}
		}
		
	}
	
	@Override
	public String toString() {
		return "TimeStamp [lamportClock=" + lamportClock + ", vectorClock="
				+ vectorClock + "]";
	}
}