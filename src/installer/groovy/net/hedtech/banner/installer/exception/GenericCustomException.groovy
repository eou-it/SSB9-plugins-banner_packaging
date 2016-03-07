package net.hedtech.banner.installer.exception

/**
 * Created by mohitj on 8/13/15.
 */
class GenericCustomException  extends RuntimeException {

    private String message = null;

    public GenericCustomException(String message) {
        super(message);
        this.message = message;
    }

    public GenericCustomException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
