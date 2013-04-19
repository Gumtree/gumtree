package au.gov.ansto.bragg.kookaburra.experiment.util;

public class ScriptStringBuilder {

	private int indentSize;
	
	private StringBuilder builder;

	public ScriptStringBuilder() {
		this(4);
	}
	
	public ScriptStringBuilder(int indentSize) {
		this.indentSize = indentSize;
		builder = new StringBuilder();
	}
	
	public void appendEmptyLine() {
		appendEmptyLine(0);
	}
	
	public void appendEmptyLine(int indentLevel) {
		indent(indentLevel);
		builder.append('\n');
	}
	
	public void appendLine(String string) {
		appendLine(string, 0);
	}
	
	public void appendLine(String string, int indentLevel) {
		indent(indentLevel);
		builder.append(string);
		builder.append('\n');
	}
	
	public void appendComment(String comment) {
		appendComment(comment, 0);
	}
	
	public void appendComment(String comment, int indentLevel) {
		indent(indentLevel);
		builder.append("# ");
		builder.append(comment);
		builder.append('\n');
	}
	
	public void appendBlockComment(String comment) {
		builder.append("###############################################################################\n");
		builder.append("#\n");
		builder.append("# ");
		builder.append(comment);
		builder.append('\n');
		builder.append("#\n");
		builder.append("###############################################################################\n");
	}
	
	private void indent(int indentLevel) {
		for (int i = 0; i < indentLevel * indentSize; i++) {
			builder.append(' ');
		}
	}
		
	public String toString() {
		return builder.toString();
	}
	
}
