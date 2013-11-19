/**
 * 
 */
package au.gov.ansto.bragg.nbi.workbench;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.util.UIResources;
import org.w3c.dom.Element;

/**
 * @author nxi
 *
 */
public class ReactorStatusWidget extends ExtendedComposite {

	private Label valueLabel;
	private static HttpClient client;
	private boolean isExpandingEnabled = true;
	private PostMethod postMethod;
	private Thread postThread;
	private final static String SOAP_XML = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
					+ "<soap:Body xmlns:ns1=\"http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl\">"
					+ "<ns1:getReactorDisplayElement/></soap:Body></soap:Envelope>"; 
	private final static int POST_THREAD_HARTBEAT = 60000;
	/**
	 * @param parent
	 * @param style
	 */
	public ReactorStatusWidget(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);
		createWidgetArea();
	}

	private void createWidgetArea() {
		// Part 2: label
		Label label = getWidgetFactory().createLabel(this, "Reactor: ");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		// Part 3: Value
		valueLabel = getWidgetFactory().createLabel(this, "--");
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER)
				.grab(true, false).applyTo(valueLabel);
		valueLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		// Part 4: Separator
//		String labelSep = (deviceContext.unit == null) ? "" : " ";
//		label = getWidgetFactory().createLabel(this, labelSep);
		label = getWidgetFactory().createLabel(this, "MW");
		try {
			createPostThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				postMethod.releaseConnection();
				postMethod = null;
				if (postThread.isAlive()) {
					postThread.interrupt();
					postThread = null;
				}
			}
		});
	}

	private void createPostThread() throws UnsupportedEncodingException {
		postMethod = new PostMethod("http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort?invoke=");
		postMethod.setDoAuthentication(false);
		RequestEntity entity = new StringRequestEntity(SOAP_XML, "text/xml", "ISO-8859-1");
		postMethod.setRequestEntity(entity);
		final Composite composite = this;
		postThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (!composite.isDisposed()) {
						updateValue();
						try {
							Thread.sleep(POST_THREAD_HARTBEAT);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							break;
						} 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		postThread.start();
	}

	private void updateValue() throws HttpException, IOException  {
        // consult documentation for your web service
//		postMethod.setRequestHeader("SOAPAction", strSoapAction);
		int statusCode = getClient().executeMethod(postMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("HTTP GET failed: " + postMethod.getStatusLine());
//			postMethod.releaseConnection();
			setValueLabel("Connection Error");
		} else {
			try{
				SOAPMessage message = MessageFactory.newInstance().createMessage();  
				SOAPPart soapPart = message.getSOAPPart();  
				soapPart.setContent(new StreamSource(postMethod.getResponseBodyAsStream()));
				//        NodeList items = soapPart.getElementsByTagName("ns0:getReactorPowerResponseElement");
				//        Element item = soapPart.getElementById("ns1:value");
				final Element ele = soapPart.getDocumentElement();
				setValueLabel(ele.getElementsByTagName("ns1:reactorPower").item(0).getTextContent());
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {

						Composite parent = getParent();
						if (parent instanceof PGroup) {
							if (isExpandingEnabled() && !((PGroup) parent).getExpanded()) {
								((PGroup) parent).setExpanded(true);
							}
						}
					}
				});

			} catch (Exception e) {
				setValueLabel("Error");
			}
		}
		postMethod.releaseConnection();
	}
	
	private void setValueLabel(final String value){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				valueLabel.setText(value);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.data.ui.viewers.ExtendedComposite#disposeWidget()
	 */
	@Override
	protected void disposeWidget() {
	}

	private HttpClient getClient() {
		if (client == null) {
			synchronized (ReactorStatusWidget.class) {
				if (client == null) {
					client = new HttpClient();
					client.getParams().setAuthenticationPreemptive(false);
				}
			}
		}
		return client;
	}

	/**
	 * @return the isExpandingEnabled
	 */
	public boolean isExpandingEnabled() {
		return isExpandingEnabled;
	}


	/**
	 * @param isExpandingEnabled the isExpandingEnabled to set
	 */
	public void setExpandingEnabled(boolean isExpandingEnabled) {
		this.isExpandingEnabled = isExpandingEnabled;
	}

	@Override
	public void dispose() {
		postMethod.releaseConnection();
		postMethod = null;
		postThread.interrupt();
		super.dispose();
	}
}
