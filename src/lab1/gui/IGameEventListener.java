package lab1.gui;

import lab1.events_game.IGameEvent;

public interface IGameEventListener {
	public void onGameEvent(IGameEvent event);
}
