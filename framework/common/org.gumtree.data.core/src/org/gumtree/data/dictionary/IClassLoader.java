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


package org.gumtree.data.dictionary;

import org.gumtree.data.interfaces.IModelObject;

/**
 * The IClassLoader aims to provide a mechanism permitting to dynamically
 * load a class that is needed by the plug-in. And then invoke a method belonging
 * to that class.
 * Sometimes data need to be processed before returning it to the CDMA interfaces.
 * Such data will be managed by an external class in a package. The implementing
 * IClassLoader must be able to retrieve and execute that class which is plug-in dependent.
 */
public interface IClassLoader extends IModelObject {
    /**
     * Execute the method that is given using it's namespace. The corresponding
     * class will be searched, loaded and instantiated, so the method can be called.
     * 
     * @param methodNameSpace full namespace of the method (package + class + method name)
     * @param context of the CDMA status while invoking the so called method
     * @return List of IObject that have been created using the called method.
     * @throws Exception in case of any trouble
     * 
     * @note the method's namespace must be that form: my.package.if.any.MyClass.MyMethod
     */
    public Object invoke( String methodNameSpace, IContext context ) throws Exception;
}
