package org.gumtree.jython.ui.internal;

public enum JythonType {
	MODULE("module"),
	CLASS("class"),
	INT("int"),
	LONG("long"),
	FLOAT("float"),
	BOOLEAN("boolean"),
	STRING("str"),
	NONE("none"),
	BUILTIN_FUNCTION("builtin_function_or_method"),
	FUNCTION("function"),
	TYPE("type"),
	LIST("list"),
	TUPLE("tuple"),
	UNKNOWN("unknow"),
	PY_JMETHOD("PyJmethod"),
	PY_JOBJECT("PyJobject");
	
	private JythonType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
	public static JythonType getType(String type) {
		for (JythonType jepType : JythonType.values()) {
			if (jepType.getType().equals(type)) {
				return jepType;
			}
		}
		return UNKNOWN;
	}
	private String type;
}
