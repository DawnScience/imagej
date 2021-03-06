/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.plugins.scripting.jython;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

/**
 * A {@link Bindings} wrapper around Jython's local variables.
 * 
 * @author Johannes Schindelin
 */
public class JythonBindings implements Bindings {

	protected final PythonInterpreter interpreter;

	public JythonBindings(final PythonInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public int size() {
		return interpreter.getLocals().__len__();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		for (final Object value2 : values()) {
			if (value.equals(value2)) return true;
		}
		return false;
	}

	@Override
	public Object get(Object key) {
		try {
			return interpreter.get((String)key);
		} catch (Error e) {
			return null;
		}
	}

	@Override
	public Object put(String key, Object value) {
		final Object result = get(key);
		try {
			interpreter.set(key, value);
		} catch (Error e) {
			// ignore
		}
		return result;
	}

	@Override
	public Object remove(Object key) {
		final Object result = get(key);
		if (result != null) interpreter.getLocals().__delitem__((String)key);
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		for (final Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	private PyStringMap dict() {
		return (PyStringMap)interpreter.getLocals();
	}

	@Override
	public void clear() {
		dict().clear();
	}

	@Override
	public Set<String> keySet() {
		final Set<String> result = new HashSet<String>();
		for (final Object name : dict().keys()) {
			result.add(name.toString());
		}
		return result;
	}

	@Override
	public Collection<Object> values() {
		final List<Object> result = new ArrayList<Object>();
		for (final Object name : dict().keys()) try {
			result.add(get(name));
		} catch (Error exc) {
			// ignore for now
		}
		return result;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		final Set<Entry<String, Object>> result = new HashSet<Entry<String, Object>>();
		for (final Object name : dict().keys()) {
			result.add(new Entry<String, Object>() {

				@Override
				public String getKey() {
					return name.toString();
				}

				@Override
				public Object getValue() {
					return get(name);
				}

				@Override
				public Object setValue(Object value) {
					throw new UnsupportedOperationException();
				}
			});
		}
		return result;
	}

}
