package au.gov.ansto.bragg.nbi.server.notebook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gumtree.service.pdf.HtmlPDFException;
import org.gumtree.service.pdf.HtmlPDFService;

public class NotebookPDFService {

	private final static String[] CSS_FILENAMES = new String[]{"raptor-front-end.css", "theme.css", "theme-icons.css", "notebook.css"};
	private final static String HTML_HEADER = "<html><head><title>ACNS Instrument Notebook</title></head><body>" +
    		"<div class=\"class_editable_page\" id=\"id_editable_page\" data-id=\"data_editable_page\">";
	private final static String HTML_FOOTER = "</div></body></html>";
	
	private HtmlPDFService pdfService;
	
	public NotebookPDFService() {
		pdfService = new HtmlPDFService();
	}
	
	public NotebookPDFService(String cssFolder) {
		this();
//		String cssFolderPath = System.getProperty(CSS_FOLDER);
		if (cssFolder != null) {
			for (String filename : CSS_FILENAMES) {
				File cssFile = new File(cssFolder + "/" + filename);
				if (cssFile.exists()) {
					try {
						pdfService.addCssFile(cssFile.toURI());
					} catch (HtmlPDFException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void addCssFile(String cssFilename) {
		File cssFile = new File(cssFilename);
		if (cssFile.exists()) {
			try {
				pdfService.addCssFile(cssFile.toURI());
			} catch (HtmlPDFException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean createPDF(String sourceFilename, String imageFolder, String targetFilename) throws HtmlPDFException, IOException {
		String htmlString = new String(Files.readAllBytes(Paths.get(sourceFilename)));
		htmlString = htmlString.replaceAll("<br>", "<p/>");
		return pdfService.createPdf(HTML_HEADER + htmlString + HTML_FOOTER, imageFolder, targetFilename);
	}
}
