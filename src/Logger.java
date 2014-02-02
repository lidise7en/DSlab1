/* 18-842 Distributed Systems
 * Lab 1
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;


public class Logger implements Runnable {
	/*
	 * Command Type:
	 * quit: quit the whole process
	 * ps: print the information of current MessagePasser
	 * send command: dest kind data
	 * receive command: receive
	 */
	private MessagePasser msgPasser;
	HashMap<String, ArrayList<LoggedMessage>> eventMap = new HashMap<String, ArrayList<LoggedMessage>>();
	private ArrayList<LoggedMessage> startMsgs = new ArrayList<LoggedMessage>();
	boolean isLogical;
	
	private Logger(MessagePasser msgPasser) {
		Thread receiverThread;
		this.msgPasser = msgPasser;
		this.isLogical = msgPasser.getIsLogical();
		/* Start the receiver thread */
		receiverThread = new Thread(this, "Receiver thread");
		receiverThread.start();
	}
	
	@SuppressWarnings("unchecked")
	private void logNewEvent(TimeStampedMessage msg) {
		
		String src;
		ArrayList<LoggedMessage> eventList = null;
		LoggedMessage newLoggedMsg = new LoggedMessage(msg);
		src = msg.getSrc();
		
		eventList = eventMap.get(src);
		if (eventList == null) {
			eventList = new ArrayList<LoggedMessage>();
			eventMap.put(src, eventList);
		}
		
		eventList.add(newLoggedMsg);
	}
	

	private void dumpEventMaps() {
		
		ArrayList<LoggedMessage> loggedMsgs;
		
		System.out.println("[LOGGER]: Dump Events Start");
		
		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			
			System.out.println("\t =============== key: " + mapEntry.getKey() + " =======================");
			loggedMsgs = mapEntry.getValue();
			
			for (LoggedMessage loggedMsg : loggedMsgs) {
				//loggedMsg.dumpLoggedMsg();
				System.out.println(loggedMsg.toString());
			}
			
			System.out.println("\t ======================================");
		}
		
		System.out.println("[LOGGER]: Dump Events End");
	}
	
	/*
	private void generateSequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			System.out.println("\t [LOGGER]: GenSeq key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();
			
			firstLoggedMsg = loggedMsgs.get(0);
			System.out.println("[LOGGER]: getSequence EVENT: " + firstLoggedMsg.getTSMsg().getMsgTS().toString());
			getSequence(firstLoggedMsg);
		}
	}
	*/
	
	private void generateSequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
//System.out.println("\t [LOGGER]: GenSeq key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();

			firstLoggedMsg = loggedMsgs.get(0);
			appendSequenceNextEvent(startMsgs, firstLoggedMsg);
//System.out.println("[LOGGER]: Genseq key: " + mapEntry.getKey() + " DONE");
		}
		
		/* We have got first level messages now lets get the sequence */
		for (LoggedMessage msgIter: startMsgs) {
			firstLoggedMsg = msgIter;
			getSequence(firstLoggedMsg);
		}
	}
		
	/*
	private void displaySequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			
			System.out.println("\t Disp Seq key: " + mapEntry.getKey());
			
			loggedMsgs = mapEntry.getValue();

			firstLoggedMsg = loggedMsgs.get(0);
			displaySequence(firstLoggedMsg);
		}
	}
	*/
	private void displaySequence()
	{
		LoggedMessage firstLoggedMsg;
		
		System.out.println("==============================");
		for (LoggedMessage msgIter: startMsgs) {
			firstLoggedMsg = msgIter;
			displaySequence(firstLoggedMsg);
		}
	}
	
	private void displayLogicalSequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();
			System.out.println("==============================");
			for (LoggedMessage loggedMsg : loggedMsgs) {
				System.out.println(loggedMsg.getTSMsg().getMsgTS().toString());
			}
		}
	}
	
	private void getSequence(LoggedMessage startEvent)
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage nextMsg = null;
		
		/*
		 *  Get next level sequence form all the hash buckets.
		 */
		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			
			nextMsg = getSequenceNextEvent(mapEntry, startEvent);
			if (nextMsg != null) {
				appendSequenceNextEvent(startEvent.getNextMsgs(), nextMsg);
			}
		}

		/* 
		 * Lets iterate through all the next level events to get there successors
		 */
		for (LoggedMessage loggedMsg: startEvent.getNextMsgs()) {
			getSequence(loggedMsg);
		}
	}

	/*
	private void displaySequence(LoggedMessage startEvent)
	{

		System.out.println(startEvent.toString());

		for (LoggedMessage loggedMsg: startEvent.getNextMsgs()) {
			displaySequence(loggedMsg);
			System.out.println("===========================================");
		}
	}
	 */
	private void displaySequence(LoggedMessage startEvent)
	{
		Iterator<LoggedMessage> it = startEvent.getNextMsgs().iterator();
		LoggedMessage loggedMsg;
		
		System.out.println(startEvent.toString());
		
		while (it.hasNext()) {
			loggedMsg = it.next();
			displaySequence(loggedMsg);
			System.out.println("===========================================");
			if (it.hasNext()) {
				/* We are changing the path in the sequence print parent node again */
				System.out.println(startEvent.toString());
			}
		}
	}
	
	private void cleanupSequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();

			for (LoggedMessage loggedMsg : loggedMsgs) {
				loggedMsg.nextMsgs.clear();
			}
		}
		
		startMsgs.clear();
	}
	
	private void cleanupAll()
	{
		ArrayList<LoggedMessage> loggedMsgs;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			loggedMsgs = mapEntry.getValue();

			for (LoggedMessage loggedMsg : loggedMsgs) {
				loggedMsg.nextMsgs.clear();
			}
			
			loggedMsgs.clear();
		}
		
		eventMap.clear();
		startMsgs.clear();
	}
	
	@SuppressWarnings("unchecked")
	private LoggedMessage getSequenceNextEvent(Entry<String, ArrayList<LoggedMessage>> mapEntry, LoggedMessage curEvent)
	{
		LoggedMessage retMsg = null;
		ArrayList<LoggedMessage> loggedMsgs;
		TimeStampedMessage loggedTSMsg = null;
		TimeStamp loggedTS = null;
		TimeStamp curTS = curEvent.getTSMsg().getMsgTS();
		
		loggedMsgs = mapEntry.getValue();
		for (LoggedMessage loggedMsg : loggedMsgs) {
			
			loggedTS = loggedMsg.getTSMsg().getMsgTS();
			
			if (curTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				retMsg = loggedMsg;
				break;
			}
		}
		
		return retMsg;
	}
	
	/*
	private void appendSequenceNextEvent(ArrayList<LoggedMessage> nextMsgs, LoggedMessage nextMsg)
	{	
		TimeStamp loggedTS = null;
		TimeStamp nextTS = nextMsg.getTSMsg().getMsgTS();
		LoggedMessage removeMessage = null;
		
		for (LoggedMessage loggedMsg : nextMsgs) {
			
			loggedTS = loggedMsg.getTSMsg().getMsgTS();
			if (nextTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				removeMessage = loggedMsg;
				break;
			}
		}
		
		if (removeMessage != null) {
			nextMsgs.remove(removeMessage);
		}
		
		System.out.println("[LOGGER] APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
		nextMsgs.add(nextMsg);
		return;
	}
	*/
	
	private void appendSequenceNextEvent(ArrayList<LoggedMessage> nextMsgs, LoggedMessage nextMsg)
	{	
		TimeStamp loggedTS = null;
		TimeStamp nextTS = nextMsg.getTSMsg().getMsgTS();
		LoggedMessage removeMessage = null;
		boolean isConcurrent = true;
		
		for (LoggedMessage loggedMsg : nextMsgs) {
			
			loggedTS = loggedMsg.getTSMsg().getMsgTS();
			if (nextTS.compare(loggedTS) == TimeStampRelation.lessEqual) {
				removeMessage = loggedMsg;
				break;
			}
		}
		
		if (removeMessage != null) {
//System.out.println("[LOGGER] REMOVE msg: " + removeMessage.getTSMsg().getMsgTS().toString());
			nextMsgs.remove(removeMessage);
		}
		
		/* 
		 * We'll add this message only if it is not concurrent with
		 * existing messages. 
		 */
		for (LoggedMessage loggedMsg : nextMsgs) {

			loggedTS = loggedMsg.getTSMsg().getMsgTS();
			if (nextTS.compare(loggedTS) != TimeStampRelation.concurrent) {
				isConcurrent = false;
				break;
			}
		}
		
		if (isConcurrent == true) {
//System.out.println("[LOGGER] APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
			nextMsgs.add(nextMsg);
		} else {
//System.out.println("[LOGGER] NO APPEND msg: " + nextMsg.getTSMsg().getMsgTS().toString());
		}
		
		return;
	}
	
	public void run() 
	{
		TimeStampedMessage msg = null;
		
		System.out.println("[LOGGER]: Started the new thread");
		
		while (true) {
			msg = (TimeStampedMessage)(this.msgPasser.receiveLogger());
    		if(msg != null) {
//System.out.println("[LOGGER]: Received a new message");
//msg.dumpMsg();
    			this.logNewEvent(msg);
    		}
		}
	}
	
	public void executing() {
		String cmdInput = new String();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Message msg = null;
        
        while (!cmdInput.equals("quit")) {
            System.out.print("CommandLine% ");
            try {
                cmdInput = in.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if(cmdInput.equals("quit")) {
            	
            	try {
					this.msgPasser.closeAllSockets();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	System.exit(0);
            	
            } else if(cmdInput.equals("ps")) {
            	
            	this.dumpEventMaps();
            	
            } else if (cmdInput.equals("sequence")) {

            	if (this.isLogical == false) {
            		this.generateSequence();
            		this.displaySequence();
            		this.cleanupSequence();
            	} else {
            		this.displayLogicalSequence();
            	}
            	
            } else if (cmdInput.equals("concurrent")) {
            	
            	this.printConcurrent();
            	
            } else if (cmdInput.equals("cleanup")) {
//System.out.println("we clean up the logger");
            	this.msgPasser.cleanUp();
            	this.cleanupAll();

            } else if (!cmdInput.equals(null) && !cmdInput.equals("\n")) {
            	
            	String[] array = cmdInput.split(" ");
            	if(array.length == 3)
            		this.msgPasser.send(new TimeStampedMessage(array[0], array[1], array[2], null));
            	else if(cmdInput.equals("receive")) {
            		msg = this.msgPasser.receiveLogger();
            		if(msg == null) {
            			System.out.println("Nothing to pass to Aplication!");
            		}
            		else {
            			System.out.println("We receive");
           				System.out.println(msg.toString());
            		}
            	}
            	else {
            		System.out.println("Invalid Command!");
            	}
            	
            } else {
            	
            	System.out.println("Invalid Command!");
            }
        }
	}
	
	public void printConcurrent() {
		for(String e : this.eventMap.keySet()) {
			for(LoggedMessage f : this.eventMap.get(e)) {
				TimeStamp ts = f.msg.getMsgTS();
				System.out.println("\n" + f.msg.getMsgTS().toString() + " " + "concurrent messages : ");
				for(String str : this.eventMap.keySet()) {
					for(LoggedMessage lm : this.eventMap.get(str)) {
						if(ts.compare(lm.msg.getMsgTS()) == TimeStampRelation.concurrent) {
							System.out.println(lm.msg.getMsgTS().toString());
						}
					}
				}
				System.out.println("====================================");
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Arguments mismtach.\n" +
					"Required arguments - <Yaml config> <name of host>");
			System.exit(0);
		}
		System.out.println("MAIN Logger");
		MessagePasser msgPasser = new MessagePasser(args[0], args[1]);
		/* Start the receiver thread */
		Logger logger = new Logger(msgPasser);
		logger.executing();
	}
}