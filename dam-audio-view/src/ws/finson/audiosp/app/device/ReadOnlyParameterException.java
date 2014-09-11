package ws.finson.audiosp.app.device;


/**
 * Thrown to indicate that the program is trying to set a device parameter
 * that is marked as not writable (ie, read only).
 *
 * @author Doug Johnson
 * @since Sep 10, 2014
 *
 */
@SuppressWarnings("serial")
public class ReadOnlyParameterException extends UnsupportedOperationException {
    /**
     * 
     */
    public ReadOnlyParameterException() {
        super();
    }

    /**
     * @param message
     */
    public ReadOnlyParameterException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ReadOnlyParameterException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ReadOnlyParameterException(String message, Throwable cause) {
        super(message, cause);
     }

}
