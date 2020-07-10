package com.johnsproject.jgameengine;

import java.awt.AWTEvent;
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

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineKeyListener;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.event.EngineMouseListener;

public class InputEngine implements EngineListener {

	private static final long AWT_EVENT_MASK = AWTEvent.KEY_EVENT_MASK
			+ AWTEvent.MOUSE_EVENT_MASK
			+ AWTEvent.MOUSE_MOTION_EVENT_MASK
			+ AWTEvent.MOUSE_WHEEL_EVENT_MASK;
	
	private static final int KEY_HOLD = KeyEvent.KEY_LAST + 1;
	private static final int MOUSE_HOLD = KeyEvent.KEY_LAST + 2;
	
	private static class InputEvent {
		
		private final int type;
		private final KeyEvent keyEvent;
		private final MouseEvent mouseEvent;
		private final MouseWheelEvent mouseWheelEvent;
		
		public InputEvent(int type, KeyEvent keyEvent, MouseEvent mouseEvent, MouseWheelEvent mouseWheelEvent) {
			this.type = type;
			this.keyEvent = keyEvent;
			this.mouseEvent = mouseEvent;
			this.mouseWheelEvent = mouseWheelEvent;
		}
		
		public int getType() {
			return type;
		}

		public KeyEvent getKeyEvent() {
			return keyEvent;
		}
		
		public MouseEvent getMouseEvent() {
			return mouseEvent;
		}

		public MouseWheelEvent getMouseWheelEvent() {
			return mouseWheelEvent;
		}
	}
	
	private Point mouseLocation;
	private Point mouseLocationOnScreen;
	private final List<EngineKeyListener> keyListeners;
	private final List<EngineMouseListener> mouseListeners;
	private final List<MouseMotionListener> motionListeners;
	private final List<MouseWheelListener> wheelListeners;
	private final List<InputEvent> inputEvents;
	
	public InputEngine() {
		this.mouseLocation = new Point();
		this.mouseLocationOnScreen = new Point();
		this.inputEvents = new ArrayList<InputEvent>();
		this.keyListeners = new ArrayList<EngineKeyListener>();
		this.mouseListeners = new ArrayList<EngineMouseListener>();
		this.wheelListeners = new ArrayList<MouseWheelListener>();
		this.motionListeners = new ArrayList<MouseMotionListener>();
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
	
	public void initialize(EngineEvent e) {
		Toolkit.getDefaultToolkit().addAWTEventListener(handleEvent(), AWT_EVENT_MASK);
	}
	
	private AWTEventListener handleEvent() {
		return new AWTEventListener() {
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
		};
	}
	
	private void handleKeyEvent(KeyEvent e) {
		final int eventType = e.getID();
		int eventIndex;
		switch (eventType) {
		case KeyEvent.KEY_TYPED:
			eventIndex = getKeyEventIndex(e, KeyEvent.KEY_TYPED);
			if(eventIndex < 0)
				inputEvents.add(new InputEvent(eventType, e, null, null));
			break;
			
		case KeyEvent.KEY_PRESSED:
			eventIndex = getKeyEventIndex(e, KEY_HOLD);
			if(eventIndex < 0) {
				inputEvents.add(new InputEvent(eventType, e, null, null));
				inputEvents.add(new InputEvent(KEY_HOLD, e, null, null));
			}
			break;

		case KeyEvent.KEY_RELEASED:
			eventIndex = getKeyEventIndex(e, KEY_HOLD);
			if(eventIndex >= 0) {
				inputEvents.remove(eventIndex);
			}
			inputEvents.add(new InputEvent(eventType, e, null, null));
			break;
		}
	}
	
	private int getKeyEventIndex(KeyEvent e, int eventType) {
		for (int i = 0; i < inputEvents.size(); i++) {
			final InputEvent event = inputEvents.get(i);
			if(event == null)
				continue;
			if((event.getType() == eventType) && (event.getKeyEvent().getKeyCode() == e.getKeyCode())) {
				return i;
			}
		}
		return -1;
	}

	private void handleMouseEvent(MouseEvent e) {
		final int eventType = e.getID();
		inputEvents.add(new InputEvent(eventType, null, e, null));
		switch (eventType) {
		case MouseEvent.MOUSE_PRESSED:
			inputEvents.add(new InputEvent(MOUSE_HOLD, null, e, null));
			break;

		case MouseEvent.MOUSE_RELEASED:
			for (int i = 0; i < inputEvents.size(); i++) {
				final InputEvent event = inputEvents.get(i);
				if(event == null)
					continue;
				if((event.getType() == MOUSE_HOLD) && (event.getMouseEvent().getButton() == e.getButton())) {
					inputEvents.remove(i);
				}
			}
			break;
		}
	}

	private void handleMouseMotionEvent(MouseEvent e) {
		final int eventType = e.getID();
		inputEvents.add(new InputEvent(eventType, null, e, null));
	}

	private void handleMouseWheelEvent(MouseWheelEvent e) {
		final int eventType = e.getID();
		inputEvents.add(new InputEvent(eventType, null, null, e));
	}
	
	public void fixedUpdate(EngineEvent e) {
		for (int i = 0; i < inputEvents.size(); i++) {
			final InputEvent event = inputEvents.get(i);
			if(event == null)
				continue;
			switch (event.getType()) {
			case KeyEvent.KEY_PRESSED:
				for (int l = 0; l < keyListeners.size(); l++) {
					keyListeners.get(l).keyPressed(event.getKeyEvent());
				}
				break;

			case KeyEvent.KEY_RELEASED:
				for (int l = 0; l < keyListeners.size(); l++) {
					keyListeners.get(l).keyReleased(event.getKeyEvent());
				}
				break;

			case KeyEvent.KEY_TYPED:
				for (int l = 0; l < keyListeners.size(); l++) {
					keyListeners.get(l).keyTyped(event.getKeyEvent());
				}
				break;
				
			case KEY_HOLD:
				for (int l = 0; l < keyListeners.size(); l++) {
					keyListeners.get(l).keyHold(event.getKeyEvent());
				}
				break;
				
			case MouseEvent.MOUSE_PRESSED:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mousePressed(event.getMouseEvent());
				}
				break;

			case MouseEvent.MOUSE_RELEASED:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mouseReleased(event.getMouseEvent());
				}
				break;

			case MouseEvent.MOUSE_CLICKED:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mouseClicked(event.getMouseEvent());
				}
				break;
				
			case MOUSE_HOLD:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mouseHold(event.getMouseEvent());
				}
				break;

			case MouseEvent.MOUSE_EXITED:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mouseExited(event.getMouseEvent());
				}
				break;

			case MouseEvent.MOUSE_ENTERED:
				for (int l = 0; l < mouseListeners.size(); l++) {
					mouseListeners.get(l).mouseEntered(event.getMouseEvent());
				}
				break;
			case MouseEvent.MOUSE_MOVED:
				for (int l = 0; l < motionListeners.size(); l++) {
					motionListeners.get(l).mouseMoved(event.getMouseEvent());
				}
				break;

			case MouseEvent.MOUSE_DRAGGED:
				for (int l = 0; l < motionListeners.size(); l++) {
					motionListeners.get(l).mouseDragged(event.getMouseEvent());
				}
				break;
				
			case MouseWheelEvent.WHEEL_UNIT_SCROLL:
				for (int l = 0; l < wheelListeners.size(); l++) {
					wheelListeners.get(l).mouseWheelMoved(event.getMouseWheelEvent());
				}
				break;
				
			case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
				for (int l = 0; l < wheelListeners.size(); l++) {
					wheelListeners.get(l).mouseWheelMoved(event.getMouseWheelEvent());
				}
				break;
			}
			if((event.getType() != KEY_HOLD) && (event.getType() != MOUSE_HOLD)) {
				inputEvents.remove(i);
			}
		}
	}
	
	public void dynamicUpdate(EngineEvent e) { }

	public int getLayer() {
		return INPUT_ENGINE_LAYER;
	}
}
