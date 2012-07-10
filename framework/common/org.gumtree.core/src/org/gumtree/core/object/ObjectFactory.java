/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.core.object;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * ObjectFactory is an utility class for Java reflection.
 *  
 * @author Tony Lam
 * @since 1.4
 *
 */
public final class ObjectFactory {

	public static Class<?> instantiateClass(String classname) throws ClassNotFoundException {
			return Class.forName(classname);
	}
	
	public static <T> T instantiateObject(Class<T> clazz) throws ObjectCreateException {
		try {
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new ObjectCreateException(t);
		}
	}
	
	public static <T> T instantiateObject(Class<T> clazz, Class<?>[] argsClass, Object... args) throws ObjectCreateException {
		try {
			Constructor<T> constructor = clazz.getConstructor(argsClass);
			return constructor.newInstance(args);
		} catch (Throwable t) {
			throw new ObjectCreateException(t);
		}
	}
	
	public static Object instantiateObject(String classname) throws ObjectCreateException {
		try {
			Class<?> clazz = Class.forName(classname);
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new ObjectCreateException(t);
		}
	}
	
	public static Object instantiateObject(String classname, Class<?>[] argsClass, Object... args) throws ObjectCreateException {
		try {
			Class<?> clazz = Class.forName(classname);
			Constructor<?> constructor = clazz.getConstructor(argsClass);
			return constructor.newInstance(args);
		} catch (Throwable t) {
			throw new ObjectCreateException(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T instantiateObject(String classname, Class<T> clazz) throws ObjectCreateException {
		Object objectCandidate = instantiateObject(classname);
		if (clazz.isAssignableFrom(objectCandidate.getClass())) {
			return (T) objectCandidate;
		}
		throw new ClassCastException("Cannot cast " + objectCandidate.getClass().toString() + " to " + clazz.toString());
	}
	
	public static Object instantiateObject(String classname, Map<String, String> classdef) throws ObjectCreateException {
		if (classdef != null) {
			String canonicalClassname = classdef.get(classname);
			if (canonicalClassname != null) {
				// convert from alias to real class name
				return instantiateObject(canonicalClassname);
			}
		}
		// no alias available
		return instantiateObject(classname);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T instantiateObject(String classname, Map<String, String> classdef, Class<T> clazz) throws ObjectCreateException {
		Object objectCandidate = instantiateObject(classname, classdef);
		if (clazz.isAssignableFrom(objectCandidate.getClass())) {
			return (T) objectCandidate;
		}
		throw new ClassCastException("Cannot cast " + objectCandidate.getClass().toString() + " to " + clazz.toString());
	}
	
	private ObjectFactory() {
		super();
	}
	
}
