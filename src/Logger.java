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
	//private ArrayList<LoggedMessage> loggedMsgs = new ArrayList<LoggedMessage>();
	
	private Logger(MessagePasser msgPasser) {
		Thread receiverThread;
		this.msgPasser = msgPasser;
		System.out.println("Inside Logger constructor");
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
			System.out.println("\t key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();
			
			for (LoggedMessage loggedMsg : loggedMsgs) {
				loggedMsg.dumpLoggedMsg();
			}
		}
		System.out.println("[LOGGER]: Dump Events End");
	}
	
	private void generateSequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			System.out.println("\t key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();
			
			firstLoggedMsg = loggedMsgs.get(0);
			getSequence(firstLoggedMsg);
		}
	}
	
	private void displaySequence()
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage firstLoggedMsg;

		for (Entry<String, ArrayList<LoggedMessage>> mapEntry: eventMap.entrySet()) {
			System.out.println("\t key: " + mapEntry.getKey());
			loggedMsgs = mapEntry.getValue();

			firstLoggedMsg = loggedMsgs.get(0);
			getSequence(firstLoggedMsg);
		}
		
	}
	
	private void getSequence(LoggedMessage startEvent)
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage nextMsg = null;

		System.out.println("[LOGGER]: Dump Events Start");
		
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

		System.out.println("[LOGGER]: Dump Events End");
	}

	private void displaySequence(LoggedMessage startEvent)
	{
		ArrayList<LoggedMessage> loggedMsgs;
		LoggedMessage nextMsg = null;
		
		startEvent.dumpLoggedMsg();

		/* 
		 * Lets iterate through all the next level events to get there successors
		 */
		for (LoggedMessage loggedMsg: startEvent.getNextMsgs()) {
			displaySequence(loggedMsg);
		}
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
		
		nextMsgs.add(nextMsg);
		return;
	}
	
	public void run() 
	{
		TimeStampedMessage msg = null;
		
		System.out.println("[LOGGER]: Started the new thread");
		
		while (true) {
			msg = (TimeStampedMessage)(this.msgPasser.receiveLogger());
    		if(msg != null) {
    			System.out.println("[LOGGER]: Received a new message");
    			msg.dumpMsg();
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
            	
            }  else if (cmdInput.equals("sequence")) {

            	generateSequence();
            	displaySequence();


            } else if (cmdInput.equals("concurrent")) {
            	
            	this.printConcurrent();

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
				System.out.println(f.msg.toString() + " " + "concurrent message : ");
				for(String str : this.eventMap.keySet()) {
					for(LoggedMessage lm : this.eventMap.get(str)) {
						if(ts.compare(lm.msg.getMsgTS()) == TimeStampRelation.concurrent) {
							System.out.println(lm.msg.toString());
						}
					}
				}
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