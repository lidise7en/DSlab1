import java.util.HashMap;


public class VectorClockService extends ClockService {

	public VectorClockService(TimeStamp newTS) {
		super(newTS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateTS(TimeStamp ts) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> mapOne = ts.getVectorClock();
		HashMap<String, Integer> mapTwo = this.getTs().getVectorClock();
		for(String e : ts.getVectorClock().keySet()) {
			if(mapOne.get(e) > mapTwo.get(e)) {
				mapTwo.put(e, mapOne.get(e));
			}
		}
	}

}
