
public class LogicalClockService extends ClockService {

	public LogicalClockService(TimeStamp newTS) {
		super(newTS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateTS(TimeStamp ts) {
		// TODO Auto-generated method stub
		if(ts.getLamportClock() > this.getTs().getLamportClock()) {
			this.setTs(ts);
		}
		
	}
	
}
