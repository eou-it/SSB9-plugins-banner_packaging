/*****************************************************************************************
 * Copyright 2009 - 2016 Ellucian Company L.P. and its affiliates.                       *
 *****************************************************************************************/
package net.hedtech.banner.installer.exception

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
