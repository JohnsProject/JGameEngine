package com.johnsproject.jgameengine;

public class MultipleParentException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new MultipleParentException with a error message
	 * that tells that the specified {@link EngineObject} has more than one parent.
	 * 
	 * @param engineObject The engine object with multiple parents.
	 */
	public MultipleParentException(EngineObject engineObject) {
		super("EngineObject has more than one parent: " + engineObject.getName());
	}

	/**
	 * Creates a new MultipleParentException with the specified error message.
	 * 
	 * @param errorMessage The message.
	 */
	public MultipleParentException(String errorMessage) {
		super(errorMessage);
	}
	
}
