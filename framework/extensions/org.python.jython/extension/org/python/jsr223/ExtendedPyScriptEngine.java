/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.python.jsr223;

import java.lang.reflect.Method;
import org.python.core.*;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.python.util.InteractiveInterpreter;


public class ExtendedPyScriptEngine extends AbstractScriptEngine implements Compilable, Invocable {
    
	// [Tony][2010-10-11] Expose as protected
    protected final InteractiveInterpreter interp;
    private final ScriptEngineFactory factory;
    private final PyModule module;
    
    public ExtendedPyScriptEngine(ScriptEngineFactory factory) {
        this(factory, new SimpleScriptContext());
    }

    public ExtendedPyScriptEngine(ScriptEngineFactory factory, ScriptContext scriptContext) {
    	setContext(scriptContext);
    	this.factory = factory;
        interp = new InteractiveInterpreter(new PyScriptEngineScope(this, scriptContext));
        module = (PyModule)Py.getSystemState().modules.__finditem__("__main__");
    }
    
    public Object eval(String script, ScriptContext context) throws ScriptException {
    	// [Tony][2010-01-07] Run directly for single line script evaluation
    	// See: http://docs.codehaus.org/display/GUMTREE/Notes+on+Jython+Support
    	try {
    		interp.setIn(context.getReader());
    		interp.setOut(context.getWriter());
    		interp.setErr(context.getErrorWriter());
    		return interp.runsource(script, "<script>");
    	} catch (PyException pye) {
            throw scriptException(pye);
        }
//        return eval(compileScript(script, context), context);
    }

    private Object eval(PyCode code, ScriptContext context) throws ScriptException {
        try {
            interp.setIn(context.getReader());
            interp.setOut(context.getWriter());
            interp.setErr(context.getErrorWriter());
            interp.setLocals(new PyScriptEngineScope(this, context));
            return interp.eval(code).__tojava__(Object.class);
        } catch (PyException pye) {
            throw scriptException(pye);
        }
    }

    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(compileScript(reader, context), context);
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public ScriptEngineFactory getFactory() {
        return factory;
    }

    public CompiledScript compile(String script) throws ScriptException {
        return new PyCompiledScript(compileScript(script, context));
    }

    public CompiledScript compile(Reader reader) throws ScriptException {
        return new PyCompiledScript(compileScript(reader, context));
    }

    private PyCode compileScript(String script, ScriptContext context) throws ScriptException {
        try {
            String filename = (String) context.getAttribute(ScriptEngine.FILENAME);
            if (filename == null) {
                return interp.compile(script);
            } else {
                return interp.compile(script, filename);
            }
        } catch (PyException pye) {
            throw scriptException(pye);
        }
    }

    private PyCode compileScript(Reader reader, ScriptContext context) throws ScriptException {
        try {
            String filename = (String) context.getAttribute(ScriptEngine.FILENAME);
            if (filename == null) {
                return interp.compile(reader);
            } else {
                return interp.compile(reader, filename);
            }
        } catch (PyException pye) {
            throw scriptException(pye);
        }
    }

    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException,
            NoSuchMethodException {
        try {
            interp.setLocals(new PyScriptEngineScope(this, context));
            if (!(thiz instanceof PyObject)) {
                thiz = Py.java2py(thiz);
            }
            PyObject method = ((PyObject) thiz).__findattr__(name);
            if (method == null) {
                throw new NoSuchMethodException(name);
            }
            //return method.__call__(Py.javas2pys(args)).__tojava__(Object.class);
            PyObject result;
            if(args != null) {
               result = method.__call__(Py.javas2pys(args));
            } else {
               result = method.__call__();
            }
            return result.__tojava__(Object.class);
        } catch (PyException pye) {
            throw scriptException(pye);
        }
    }

    public Object invokeFunction(String name, Object... args) throws ScriptException,
            NoSuchMethodException {
        try {
            interp.setLocals(new PyScriptEngineScope(this, context));
            PyObject function = interp.get(name);
            if (function == null) {
                throw new NoSuchMethodException(name);
            }
            return function.__call__(Py.javas2pys(args)).__tojava__(Object.class);
        } catch (PyException pye) {
            throw scriptException(pye);
        }
    }

    public <T> T getInterface(Class<T> clazz) {
        return getInterface(new PyModule("__jsr223__", interp.getLocals()), clazz);
    }

    public <T> T getInterface(Object obj, Class<T> clazz) {
        if (obj == null) {
            throw new IllegalArgumentException("object expected");
        }
        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("interface expected");
        }
        interp.setLocals(new PyScriptEngineScope(this, context));
        final PyObject thiz = Py.java2py(obj);
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[] { clazz },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    try {
                        interp.setLocals(new PyScriptEngineScope(ExtendedPyScriptEngine.this, context));
                        PyObject pyMethod = thiz.__findattr__(method.getName());
                        if (pyMethod == null)
                            throw new NoSuchMethodException(method.getName());
                        PyObject result = pyMethod.__call__(Py.javas2pys(args));
                        return result.__tojava__(Object.class);
                    } catch (PyException pye) {
                        throw scriptException(pye);
                    }
                }
            });
        return proxy;
    }

    private static ScriptException scriptException(PyException pye) {
        ScriptException se = null;
        try {
            pye.normalize();

            PyObject type = pye.type;
            PyObject value = pye.value;
            PyTraceback tb = pye.traceback;

            if (__builtin__.isinstance(value, Py.SyntaxError)) {
                PyObject filename = value.__findattr__("filename");
                PyObject lineno = value.__findattr__("lineno");
                PyObject offset = value.__findattr__("offset");
                value = value.__findattr__("msg");

                se = new ScriptException(
                        Py.formatException(type, value),
                        filename == null ? "<script>" : filename.toString(),
                        lineno == null ? 0 : lineno.asInt(),
                        offset == null ? 0 : offset.asInt());
            } else if (tb != null) {
                String filename;
                if (tb.tb_frame == null || tb.tb_frame.f_code == null) {
                    filename = null;
                } else {
                    filename = tb.tb_frame.f_code.co_filename;
                }
                se = new ScriptException(
                        Py.formatException(type, value),
                        filename,
                        tb.tb_lineno);
            } else {
                se = new ScriptException(Py.formatException(type, value));
            }
            se.initCause(pye);
            return se;
        } catch (Exception ee) {
            se = new ScriptException(pye);
        }
        return se;
    }

    private class PyCompiledScript extends CompiledScript {
        private PyCode code;

        PyCompiledScript(PyCode code) {
            this.code = code;
        }

        @Override
        public ScriptEngine getEngine() {
            return ExtendedPyScriptEngine.this;
        }

        @Override
        public Object eval(ScriptContext ctx) throws ScriptException {
            return ExtendedPyScriptEngine.this.eval(code, ctx);
        }
    }
}
