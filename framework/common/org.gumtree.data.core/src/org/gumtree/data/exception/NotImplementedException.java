/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.exception;


public class NotImplementedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6906360679955876972L;
    private static final String MESSAGE = "not supported yet in plug-in!";

    /**
     * 
     */
    public NotImplementedException() {
        super(MESSAGE);
    }

    /**
     * @param cause Throwable object
     */
    public NotImplementedException(final Throwable cause) {
        super(MESSAGE, cause);
    }


}
