package au.gov.ansto.bragg.quokka.msw.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.wb.swt.SWTResourceManager;

public final class ScriptCodeFont {
	// construction
	private ScriptCodeFont() {
	}
	
	// methods
	public static Font get() {
		return SWTResourceManager.getFont("Consolas", 10, SWT.NONE);
	}
}
