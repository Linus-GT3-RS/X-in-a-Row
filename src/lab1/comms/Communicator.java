package lab1.comms;

import java.util.HashSet;
import java.util.Set;

import lab1.cmds_comms.ICommunicationCommand;
import lab1.events_comms.ICommunicationEvent;
import lab1.game.Utils;

public abstract class Communicator {

	private Set<ICommunicationEventListener> commEventListeners;
	
	public Communicator() {
		this.commEventListeners = new HashSet<ICommunicationEventListener>();
	}
	
	public void addCommEventListener(ICommunicationEventListener listener) {
		commEventListeners.add(listener);
		Utils.log("Added new CommEvent listener");
	}
	
	protected void sendCommEvent(ICommunicationEvent ev) {
		commEventListeners.forEach(list -> list.onCommEvent(ev));
	}
	
	public abstract void processCommCmd(ICommunicationCommand cmd);
	
}
