package com.johnsproject.jpge2.controller;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import com.johnsproject.jpge2.event.EngineKeyListener;
import com.johnsproject.jpge2.event.EngineListener;
import com.johnsproject.jpge2.event.EngineMouseListener;

public class InputController implements EngineListener {

	private Point mouseLocation = new Point();
	private Point mouseLocationOnScreen = new Point();
	private KeyEvent[] keyEvents = new KeyEvent[8];
	private MouseEvent[] mouseEvents = new MouseEvent[4];
	private List<EngineKeyListener> keyListeners = new ArrayList<EngineKeyListener>();
	private List<EngineMouseListener> mouseListeners = new ArrayList<EngineMouseListener>();
	private List<MouseMotionListener> motionListeners = new ArrayList<MouseMotionListener>();
	private List<MouseWheelListener> wheelListeners = new ArrayList<MouseWheelListener>();
	
	public InputController() {
		this.keyListeners = new ArrayList<EngineKeyListener>();
		this.mouseListeners = new ArrayList<EngineMouseListener>();
		this.wheelListeners = new ArrayList<MouseWheelListener>();
		this.motionListeners = new ArrayList<MouseMotionListener>();
		start();
	}

	public KeyEvent[] getPressedKeys() {
		return keyEvents;
	}

	public MouseEvent[] getPressedMouseButtons() {
		return mouseEvents;
	}

	public Point getMouseLocation() {
		return mouseLocation;
	}

	public Point getMouseLocationOnScreen() {
		return mouseLocationOnScreen;
	}
	
	public void addEngineKeyListener(EngineKeyListener listener) {
		keyListeners.add(listener);
	}
	
	public void removeEngineKeyListener(EngineKeyListener listener) {
		keyListeners.remove(listener);
	}
	
	public void addEngineMouseListener(EngineMouseListener listener) {
		mouseListeners.add(listener);
	}
	
	public void removeEngineMouseListener(EngineMouseListener listener) {
		mouseListeners.remove(listener);
	}
	
	public void addMouseWheelListener(MouseWheelListener listener) {
		wheelListeners.add(listener);
	}
	
	public void removeMouseWheelListener(MouseWheelListener listener) {
		wheelListeners.remove(listener);
	}
	
	public void addMouseMotionListener(MouseMotionListener listener) {
		motionListeners.add(listener);
	}
	
	public void removeMouseMotionListener(MouseMotionListener listener) {
		motionListeners.remove(listener);
	}
	
	public void start() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent) {
					KeyEvent e = (KeyEvent) event;
					handleKeyEvent(e);
				}
				if (event instanceof MouseEvent) {
					MouseEvent e = (MouseEvent) event;
					mouseLocation = e.getPoint();
					mouseLocationOnScreen = e.getLocationOnScreen();
					handleMouseEvent(e);
					handleMouseMotionEvent(e);
				}
				if (event instanceof MouseWheelEvent) {
					MouseWheelEvent e = (MouseWheelEvent) event;
					handleMouseWheelEvent(e);
				}
			}
		}, AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}
	
	public void update() {
		
	}

	public void fixedUpdate() {
		for (int i = 0; i < keyEvents.length; i++) {
			KeyEvent keyEvent = keyEvents[i];
			if (keyEvent != null) {
				for (int j = 0; j < keyListeners.size(); j++) {
					keyListeners.get(j).keyDown(keyEvent);
				}
			}
		}
		for (int i = 0; i < mouseEvents.length; i++) {
			MouseEvent mouseEvent = mouseEvents[i];
			if (mouseEvent != null) {
				mouseEvent = new MouseEvent((Component) mouseEvent.getSource(),
														mouseEvent.getID(),
														mouseEvent.getWhen(),
														mouseEvent.getModifiers(),
														(int) mouseLocation.getX(),
														(int) mouseLocation.getY(),
														mouseEvent.getClickCount(), false);
				for (int j = 0; j < mouseListeners.size(); j++) {
					mouseListeners.get(j).mouseDown(mouseEvent);
				}
			}
		}
	}
	
	private void handleKeyEvent(KeyEvent e) {
		switch (e.getID()) {
		case KeyEvent.KEY_PRESSED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyPressed(e);
			}
			for (int i = 0; i < keyEvents.length; i++) {
				KeyEvent keyEvent = keyEvents[i];
				if ((keyEvent != null) && (keyEvent.getKeyCode() == e.getKeyCode())) {
					return;
				}
			}
			for (int i = 0; i < keyEvents.length; i++) {
				KeyEvent keyEvent = keyEvents[i];
				if (keyEvent == null) {
					keyEvents[i] = e;
					return;
				}
				if (keyEvent.getKeyCode() == e.getKeyCode())
					return;
			}
			break;

		case KeyEvent.KEY_RELEASED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyReleased(e);
			}
			for (int i = 0; i < keyEvents.length; i++) {
				KeyEvent keyEvent = keyEvents[i];
				if ((keyEvent != null) && (keyEvent.getKeyCode() == e.getKeyCode())) {
					keyEvents[i] = null;
				}
			}
			break;

		case KeyEvent.KEY_TYPED:
			for (int i = 0; i < keyListeners.size(); i++) {
				keyListeners.get(i).keyTyped(e);
			}
			break;
		}
	}

	private void handleMouseEvent(MouseEvent e) {
		switch (e.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mousePressed(e);
			}
			for (int i = 0; i < mouseEvents.length; i++) {
				MouseEvent mouseEvent = mouseEvents[i];
				if (mouseEvent == null) {
					mouseEvents[i] = e;
					return;
				}
				if (mouseEvent.getButton() == e.getButton())
					return;
			}
			break;

		case MouseEvent.MOUSE_RELEASED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseReleased(e);
			}
			for (int i = 0; i < mouseEvents.length; i++) {
				MouseEvent mouseEvent = mouseEvents[i];
				if ((mouseEvent != null) && (mouseEvent.getButton() == e.getButton())) {
					mouseEvents[i] = null;
				}
			}
			break;

		case MouseEvent.MOUSE_CLICKED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseClicked(e);
			}
			break;

		case MouseEvent.MOUSE_EXITED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseExited(e);
			}
			break;

		case MouseEvent.MOUSE_ENTERED:
			for (int i = 0; i < mouseListeners.size(); i++) {
				mouseListeners.get(i).mouseEntered(e);
			}
			break;
		}
	}

	private void handleMouseMotionEvent(MouseEvent e) {
		switch (e.getID()) {
		case MouseEvent.MOUSE_MOVED:
			for (int i = 0; i < motionListeners.size(); i++) {
				motionListeners.get(i).mouseMoved(e);
			}
			break;

		case MouseEvent.MOUSE_DRAGGED:
			for (int i = 0; i < motionListeners.size(); i++) {
				motionListeners.get(i).mouseDragged(e);
			}
			break;
		}
	}

	private void handleMouseWheelEvent(MouseWheelEvent e) {
		for (int i = 0; i < wheelListeners.size(); i++) {
			wheelListeners.get(i).mouseWheelMoved(e);
		}
	}

	public int getPriority() {
		return 0;
	}
}
