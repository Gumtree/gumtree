package org.gumtree.gumnix.sics.internal.ui.hm;

import gov.anl.ipns.ViewTools.Components.AxisInfo;
import gov.anl.ipns.ViewTools.Components.IVirtualArray2D;
import gov.anl.ipns.ViewTools.Components.VirtualArray2D;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.vis.isaw.ISAW2DVis;

public class TestHMView extends ViewPart {

	private ISAW2DVis isaw2DVis;

	private Composite composite;

	public TestHMView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new FillLayout());

		isaw2DVis = new ISAW2DVis(composite, SWT.NONE);

		Button button = new Button(parent, SWT.PUSH);
		button.setText("Load HM");
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsCore.getDefaultProxy().send("hmm zipget -1", new SicsCallbackAdapter() {
						public void receiveRawData(final Object rawData) {
							Display display = PlatformUI.getWorkbench().getDisplay();
							if(composite != null && !composite.isDisposed() && !display.isDisposed()) {
								try {
									final float[][]decompressedData = decompressData((byte[])rawData);
									display.asyncExec(new Runnable() {
										public void run() {
											IVirtualArray2D va2D = new VirtualArray2D(decompressedData);
											va2D.setAxisInfo(AxisInfo.X_AXIS, 0, 128, "Detector Tube", "Tube Number",
													AxisInfo.LINEAR);
											va2D.setAxisInfo(AxisInfo.Y_AXIS, 0, 512, "Position", "Position",
													AxisInfo.TRU_LOG);
											va2D.setTitle("Echidna HM Data");

//											for(Control child : composite.getChildren()) {
//												child.dispose();
//											}
//
//											ISAW2DVis isaw2DVis = new ISAW2DVis(composite, SWT.NONE);
											isaw2DVis.setVirtualArray(va2D);
//											composite.layout(true);
										}
									});
								} catch (DataFormatException e) {
									e.printStackTrace();
								}
							}
						}
					}, ISicsProxy.CHANNEL_ZIP_DATA);
				} catch (SicsIOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void setFocus() {
	}

	private float[][] decompressData(byte[] rawBytes) throws DataFormatException{
		Inflater decompresser = new Inflater();
		decompresser.setInput(rawBytes, 0, rawBytes.length);
		int iSize = 512;
		int jSize = 128;
		int intSize = 4;
		byte[] result = new byte[iSize * jSize * intSize];
		float[][] data = new float[iSize][jSize];
		decompresser.inflate(result);
		decompresser.end();
		int b1, b2, b3, b4;
		for (int i = 0; i < iSize; i++) {
			for (int j = 0; j < jSize ; j++) {
				b1 = result[(i * jSize + j) * intSize];
				b2 = result[(i * jSize + j) * intSize + 1];
				b3 = result[(i * jSize + j) * intSize + 2];
				b4 = result[(i * jSize + j) * intSize + 3];
//				data[i][j] = (float) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
				// flip vertically for ISAW to display correctly
				data[iSize - i - 1][j] = (float) ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
				if(data[iSize - i - 1][j] > 0) {
					System.out.println((iSize - i - 1) + "," + j + ":" + data[iSize - i - 1][j]);
				}
			}
		}
		return data;
	}
}
