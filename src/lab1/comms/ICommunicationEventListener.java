package lab1.comms;

import lab1.events_comms.ICommunicationEvent;

public interface ICommunicationEventListener {

	public void onCommEvent(ICommunicationEvent event);
	
}
