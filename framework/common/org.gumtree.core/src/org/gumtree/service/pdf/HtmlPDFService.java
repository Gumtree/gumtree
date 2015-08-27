/**
 * 
 */
package org.gumtree.service.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;

import scala.collection.generic.VolatileAbort;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.CssResolverException;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

/**
 * @author nxi
 *
 */
public class HtmlPDFService {

	private static final int TIME_OUT_IN_MILLISECONDS = 60000;
	private CSSResolver cssResolver;
	
	/**
	 * 
	 */
	public HtmlPDFService() {
		cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
	}

	/**
	 * Add CSS file as URI. CSS files will be used to create the PDF layout.
	 * @param cssUri
	 * @throws HtmlPDFException 
	 */
	public void addCssFile(URI cssUri) throws HtmlPDFException {
		try {
			cssResolver.addCssFile(cssUri.toString(), true);
		} catch (CssResolverException e) {
			throw new HtmlPDFException(e);
		}
	}
	
	/**
	 * Remove all CSS files from the PDF writer.
	 * @param cssUri
	 * @throws HtmlPDFException 
	 */
	public void clearCssFiles(URI cssUri) throws HtmlPDFException {
		try {
			cssResolver.clear();
		} catch (CssResolverException e) {
			throw new HtmlPDFException(e);
		}
	}

    class Base64ImageProvider extends AbstractImageProvider {
    	 
        @Override
        public Image retrieve(String src) {
            int pos = src.indexOf("base64,");
            try {
                if (src.startsWith("data") && pos > 0) {
                    byte[] img = Base64.decode(src.substring(pos + 7));
                    return Image.getInstance(img);
                }
                else {
                    return Image.getInstance(src);
                }
            } catch (BadElementException ex) {
                return null;
            } catch (IOException ex) {
                return null;
            }
        }
 
        @Override
        public String getImageRootPath() {
            return null;
        }
    }

    
    /**
     * Create a PDF file from a given HTML file and a list of css files. 
     * @param htmlFile File object
     * @param targetFile String
     * @param cssUrls List of URLs of css files
     * @throws IOException
     * @throws HtmlPDFException 
     */
    public boolean createPdf(File htmlFile, String targetFile, List<File> cssFiles) throws IOException, HtmlPDFException {
    	try {
        	cssResolver.clear();
        	if (cssFiles.size() > 0) {
        		for (File cssFile : cssFiles) {
        			cssResolver.addCssFile(cssFile.toURI().toString(), true);
        		}
        	}
		} catch (CssResolverException e) {
			throw new HtmlPDFException(e);
		}
    	return createPdf(htmlFile, targetFile);
    }

    /**
     * Create a PDF file from a given HTML file. 
     * @param htmlFile
     * @param targetFile
     * @return
     * @throws IOException
     * @throws HtmlPDFException 
     */
    public boolean createPdf(File htmlFile, String targetFile) throws IOException, HtmlPDFException {
    	String htmlString = new String(Files.readAllBytes(Paths.get(htmlFile.getAbsolutePath())));
    	return createPdf(htmlString, targetFile);
    }
    
    /**
     * Create a PDF file from a given HTML text and a list of css files. 
     * @param htmlString
     * @param targetFile
     * @param cssFiles
     * @return
     * @throws FileNotFoundException
     * @throws HtmlPDFException 
     */
    public boolean createPdf(final String htmlString, final String targetFile, List<File> cssFiles) 
    		throws FileNotFoundException, HtmlPDFException {
    	try {
        	cssResolver.clear();
        	if (cssFiles.size() > 0) {
        		for (File cssFile : cssFiles) {
        			cssResolver.addCssFile(cssFile.toURI().toString(), true);
        		}
        	}			
		} catch (CssResolverException e) {
			throw new HtmlPDFException(e);
		}
    	return createPdf(htmlString, targetFile);
    }
    
    /**
     * Create a PDF file from a given HTML file. 
     * @param htmlString
     * @param targetFile
     * @return is successful flag
     * @throws FileNotFoundException
     * @throws HtmlPDFException 
     */
    public boolean createPdf(final String htmlString, final String targetFile) throws FileNotFoundException, HtmlPDFException {

              // step 1
        final Document document = new Document();
        Thread thread = null;
		try {
	        // step 2
			final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetFile));
	        // step 3
	        document.open();
	    	thread = new Thread(new Runnable() {
				
				@Override
				public void run() {

			        try {
				        // HTML
				        HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
				        htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
				        htmlContext.setImageProvider(new Base64ImageProvider());

				        // Pipelines
				        PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
				        HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
				        CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

				        // XML Worker
				        XMLWorker worker = new XMLWorker(css, true);
				        XMLParser p = new XMLParser(worker);
						p.parse(new ByteArrayInputStream(htmlString.getBytes()));
						System.err.println("finished parsing");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
				        // step 5
				        try {
				        	document.close();
						} catch (Exception e2) {
						}
					}
				}
			});
	    	thread.start();
		} catch (DocumentException e1) {
			throw new HtmlPDFException(e1);
		}
		LoopRunner.run(new ILoopExitCondition() {
			
			@Override
			public boolean getExitCondition() {
				return !document.isOpen();
			}
		}, TIME_OUT_IN_MILLISECONDS, 100);
		if (document.isOpen()) {
			try {
				thread.interrupt();
				document.close();
			} catch (Exception e) {
			}
			System.err.println("time out parsing the html");
			return false;
		}
		return true;
  }
}
