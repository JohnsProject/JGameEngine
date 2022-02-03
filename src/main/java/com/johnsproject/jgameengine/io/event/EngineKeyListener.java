package com.johnsproject.jgameengine.io.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public interface EngineKeyListener extends KeyListener {

	public void keyHold(KeyEvent e);
	
}
