
public class TimeStampedMessage extends Message{



	private TimeStamp msgTS = null;
	
	public TimeStampedMessage(String dest, String kind, Object data, TimeStamp ts) {
		super(dest, kind, data);
		this.msgTS = ts;
		// TODO Auto-generated constructor stub
	}

	public TimeStamp getMsgTS() {
		return msgTS;
	}

	public void setMsgTS(TimeStamp msgTS) {
		this.msgTS = msgTS;
	}
	public Message makeCopy() {
		Message result = new TimeStampedMessage(this.getDest(), this.getKind(), this.getData(), this.msgTS);
		result.set_source(this.getSrc());
		result.set_duplicate(this.isDuplicate());
		result.set_seqNum(this.getSeqNum());
		return result;
	}
}
