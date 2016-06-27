/*****************************************************************************************
 * Copyright 2009 - 2016 Ellucian Company L.P. and its affiliates.                       *
 *****************************************************************************************/
package net.hedtech.banner.installer.exception

import org.junit.Test

import static org.junit.Assert.assertEquals


class GenericCustomExceptionTest {

    GenericCustomException genericCustomException

    @Test
    void "genericCustomException toString test"(){
        genericCustomException  = new GenericCustomException("Test toString");

        assertEquals(genericCustomException.toString(), "Test toString");
    }

    @Test
    void "genericCustomException Message test"(){
        genericCustomException  = new GenericCustomException("Test message");

        assertEquals(genericCustomException.message, "Test message");
    }
}
