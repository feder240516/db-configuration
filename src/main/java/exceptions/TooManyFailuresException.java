package exceptions;

public class TooManyFailuresException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6330522303140398466L;

	public TooManyFailuresException() {
		super();
	}

	public TooManyFailuresException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TooManyFailuresException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyFailuresException(String message) {
		super(message);
	}

	public TooManyFailuresException(Throwable cause) {
		super(cause);
	}

	
}
