package com.johnsproject.jpge2.controller;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

public class InputController {
	
	private final List<KeyListener> keyListeners;
	private final List<MouseListener> mouseListeners;
	private final List<MouseWheelListener> mouseWheelListeners;
	private final List<MouseMotionListener> mouseMotionListeners;
	
	InputController() {
		this.keyListeners = new ArrayList<KeyListener>();
		this.mouseListeners = new ArrayList<MouseListener>();
		this.mouseWheelListeners = new ArrayList<MouseWheelListener>();
		this.mouseMotionListeners = new ArrayList<MouseMotionListener>();
		start();
	}
	
	public void addKeyListener(KeyListener listener) {
		keyListeners.add(listener);
	}
	
	public void removeKeyListener(KeyListener listener) {
		keyListeners.remove(listener);
	}
	
	public void addMouseListener(MouseListener listener) {
		mouseListeners.add(listener);
	}
	
	public void removeMouseListener(MouseListener listener) {
		mouseListeners.remove(listener);
	}
	
	public void addMouseWheelListener(MouseWheelListener listener) {
		mouseWheelListeners.add(listener);
	}
	
	public void removeMouseWheelListener(MouseWheelListener listener) {
		mouseWheelListeners.remove(listener);
	}
	
	public void addMouseMotionListener(MouseMotionListener listener) {
		mouseMotionListeners.add(listener);
	}
	
	public void removeMouseMotionListener(MouseMotionListener listener) {
		mouseMotionListeners.remove(listener);
	}
	
	private void start() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent) {
					handleEvent((KeyEvent) event);
				}else if (event instanceof MouseEvent) {
					handleEvent((MouseEvent) event);
				}else if (event instanceof MouseWheelEvent) {
					handleEvent((MouseWheelEvent) event);
				}
			}
		}, AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}
	
	
	private void handleEvent(KeyEvent e) {
		switch (e.getID()) {
		case KeyEvent.KEY_PRESSED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyPressed(e);
			}
			break;
			
		case KeyEvent.KEY_RELEASED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyReleased(e);
			}
			break;
			
		case KeyEvent.KEY_TYPED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyTyped(e);
			}
			break;
		}
	}
	
	private void handleEvent(MouseEvent e) {
		switch (e.getID()) {
		case MouseEvent.MOUSE_CLICKED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseClicked(e);
			}
			break;
			
		case MouseEvent.MOUSE_ENTERED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseEntered(e);
			}
			break;
			
		case MouseEvent.MOUSE_EXITED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseExited(e);
			}
			break;
			
		case MouseEvent.MOUSE_PRESSED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mousePressed(e);
			}
			break;
			
		case MouseEvent.MOUSE_RELEASED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseReleased(e);
			}
			break;
			
		case MouseEvent.MOUSE_DRAGGED:
			for (int i = 0; i < mouseMotionListeners.size(); i++) {
				mouseMotionListeners.get(i).mouseDragged(e);
			}
			break;
			
		case MouseEvent.MOUSE_MOVED:
			for (int i = 0; i < mouseMotionListeners.size(); i++) {
				mouseMotionListeners.get(i).mouseMoved(e);
			}
			break;
		}
	}
	
	private void handleEvent(MouseWheelEvent e) {
		for (int i = 0; i < mouseWheelListeners.size(); i++) {
			mouseWheelListeners.get(i).mouseWheelMoved(e);
		}
	}
}
