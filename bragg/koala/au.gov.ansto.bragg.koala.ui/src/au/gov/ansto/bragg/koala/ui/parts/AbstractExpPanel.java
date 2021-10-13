/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.SWTX;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel;
import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel;
import au.gov.ansto.bragg.koala.ui.scan.SingleScan;

/**
 * @author nxi
 *
 */
public abstract class AbstractExpPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1860;
	private static final int HEIGHT_HINT = 720;
	protected MainPart mainPart;
	protected KTable table;
	
	/**
	 * @param parent
	 * @param style
	 */
	public AbstractExpPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().margins(8, 8).numColumns(2).applyTo(this);
		GridDataFactory.swtDefaults().minSize(1860, 720).align(SWT.CENTER, SWT.FILL).applyTo(this);
		
		final Composite scanBlock = new Composite(this, SWT.BORDER);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(1).applyTo(scanBlock);
		GridDataFactory.fillDefaults().minSize(1200, 640).grab(true, true).align(SWT.FILL, SWT.BEGINNING).applyTo(scanBlock);
		
	    table = new KTable(scanBlock, SWT.NONE 
	    							| SWT.MULTI 
	    							| SWT.FULL_SELECTION 
	    							| SWT.V_SCROLL 
	    							| SWT.H_SCROLL
	    							| SWTX.FILL_WITH_LASTCOL
	    							| SWT.FULL_SELECTION
	    							);
	    final AbstractScanModel model = getModel();
	    model.setFont(Activator.getMiddleFont());
	    table.setFont(Activator.getMiddleFont());
	    table.setCursor(Activator.getHandCursor());
	    table.setModel(model);
	    table.setPreferredSizeDefaultRowHeight(44);
	    model.setTable(table);
	    
//	    KTableCellSelectionAdapter listener = new KTableCellSelectionAdapter() {
//	    	@Override
//	    	public void cellSelected(int col, int row, int statemask) {
//	    		if (row > 0) {
//	    			if (col == 0) {
//	    				model.insertScan(row);
//	    				table.redraw();
//	    			} else if (col == 1) {
//	    				model.deleteScan(row - 1);
//	    				table.redraw();
//	    			}
//	    		}
//	    	}
//	    	
//	    };
//	    table.addCellSelectionListener(listener);
	    GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(table);
		
	    Composite infoPart = new Composite(this, SWT.NONE);
	    GridLayoutFactory.fillDefaults().applyTo(infoPart);
	    GridDataFactory.fillDefaults().span(1, 2).grab(false, true).align(SWT.FILL, SWT.BEGINNING).minSize(480, 600).applyTo(infoPart);
	    
		final Group statusPart = new Group(infoPart, SWT.NONE);
		statusPart.setText("Current scan status");
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(statusPart);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(statusPart);
	    	    
		final Label fileLabel = new Label(statusPart, SWT.NONE);
		fileLabel.setText("Filename");
		fileLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(180, 40).applyTo(fileLabel);
		
		final Text fileText = new Text(statusPart, SWT.BORDER);
		fileText.setFont(Activator.getMiddleFont());
		fileText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(fileText);
		
	    final Button openButton = new Button(statusPart, SWT.PUSH);
	    openButton.setImage(KoalaImage.IMAGE32.getImage());
//	    openButton.setFont(Activator.getMiddleFont());
	    openButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(40, 40).applyTo(openButton);

		final Label phiLabel = new Label(statusPart, SWT.NONE);
		phiLabel.setText("Phi value");
		phiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(phiLabel);
		
		final Text phiText = new Text(statusPart, SWT.BORDER);
		phiText.setFont(Activator.getMiddleFont());
		phiText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).applyTo(phiText);
		
		final Label numLabel = new Label(statusPart, SWT.NONE);
		numLabel.setText("Step number");
		numLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		final Text numText = new Text(statusPart, SWT.BORDER);
		numText.setFont(Activator.getMiddleFont());
		numText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).applyTo(numText);
		
		final Group phasePart = new Group(infoPart, SWT.NONE);
	    phasePart.setText("Instrument Phase");
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(phasePart);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(phasePart);
	    
	    final Label erasureButton = new Label(phasePart, SWT.BORDER);
	    erasureButton.setText("Erasure");
	    erasureButton.setFont(Activator.getMiddleFont());
	    erasureButton.setBackground(Activator.getLightColor());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(erasureButton);

	    final Label expoButton = new Label(phasePart, SWT.NONE);
	    expoButton.setText("Exposure");
	    expoButton.setFont(Activator.getMiddleFont());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(expoButton);

	    final Label readButton = new Label(phasePart, SWT.NONE);
	    readButton.setText("Reading");
	    readButton.setFont(Activator.getMiddleFont());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(readButton);

		final ProgressBar proBar = new ProgressBar(phasePart, SWT.HORIZONTAL);
		proBar.setMaximum(100);
		proBar.setMinimum(0);
		proBar.setSelection(10);
//		proBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(proBar);

		final Group controlPart = new Group(infoPart, SWT.NONE);
		controlPart.setText("Control");
	    GridLayoutFactory.fillDefaults().numColumns(2).margins(4, 4).applyTo(controlPart);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(controlPart);
	    
	    final Button pauseButton = new Button(controlPart, SWT.PUSH);
	    pauseButton.setImage(KoalaImage.PAUSE64.getImage());
	    pauseButton.setText("PAUSE");
	    pauseButton.setFont(Activator.getMiddleFont());
	    pauseButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(128, 80).applyTo(pauseButton);

	    final Button stopButton = new Button(controlPart, SWT.PUSH);
	    stopButton.setImage(KoalaImage.STOP64.getImage());
	    stopButton.setText("STOP");
	    stopButton.setFont(Activator.getMiddleFont());
	    stopButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(128, 80).applyTo(stopButton);

		final ScrolledComposite batchHolder = new ScrolledComposite(infoPart, SWT.NONE);
	    GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(batchHolder);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(batchHolder);
	    batchHolder.setExpandHorizontal(true);
	    batchHolder.setExpandVertical(true);

	    final Composite emptyPart = new Composite(batchHolder, SWT.NONE);
	    
	    final Group batchGroup = new Group(batchHolder, SWT.NONE);
	    batchGroup.setText("Undate selected rows");
	    batchGroup.setBackground(Activator.getLightColor());
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(batchGroup);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(batchGroup);

		final Label comLabel = new Label(batchGroup, SWT.NONE);
		comLabel.setText("Comments");
		comLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(comLabel);
		
		final Text comText = new Text(batchGroup, SWT.BORDER);
		comText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).applyTo(comText);
		
		final Label fnLabel = new Label(batchGroup, SWT.NONE);
		fnLabel.setText("Filename");
		fnLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(fnLabel);
		
		final Text fnText = new Text(batchGroup, SWT.BORDER);
		fnText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).applyTo(fnText);

	    final Button applyButton = new Button(batchGroup, SWT.PUSH);
	    applyButton.setImage(KoalaImage.MULTI_APPLY48.getImage());
	    applyButton.setText("Apply to Selected Entries");
	    applyButton.setFont(Activator.getMiddleFont());
	    applyButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).span(3, 1).hint(128, 64).applyTo(applyButton);

		applyButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String com = comText.getText();
				String fn = fnText.getText();
				AbstractScanModel model = getModel();
				int[] rows = table.getRowSelection();
				for (int row : rows) {
					SingleScan scan = model.getItem(row);
					if (com != null && com.trim().length() > 0) {
						scan.setComments(com);
					}
					if (fn != null && fn.trim().length() > 0) {
						scan.setFilename(fn);
					}
				}
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Label estLabel = new Label(batchGroup, SWT.NONE);
		estLabel.setText("Time estimation of selected");
		estLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(2, 1).minSize(240, 40).applyTo(estLabel);
		
		final Text estText = new Text(batchGroup, SWT.BORDER);
		estText.setFont(Activator.getMiddleFont());
		estText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).minSize(180, 40).applyTo(estText);
		
		final Composite ctrPart = new Composite(batchGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).applyTo(ctrPart);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(ctrPart);
		
	    final Button dupButton = new Button(ctrPart, SWT.PUSH);
	    dupButton.setImage(KoalaImage.COPY48.getImage());
	    dupButton.setText("Duplicate");
	    dupButton.setFont(Activator.getMiddleFont());
	    dupButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(128, 64).applyTo(dupButton);

		dupButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] rows = table.getRowSelection();
				int rmax = 0;
				List<SingleScan> items = new ArrayList<SingleScan>();
				AbstractScanModel model = getModel();
				for (int row : rows) {
					items.add(model.getItem(row).getCopy());
				}
				model.addScans(rows[rows.length - 1], items);
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    final Button removeButton = new Button(ctrPart, SWT.PUSH);
	    removeButton.setImage(KoalaImage.DELETE48.getImage());
	    removeButton.setText("Remove");
	    removeButton.setFont(Activator.getMiddleFont());
	    removeButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(128, 64).applyTo(removeButton);

		removeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int[] rows = table.getRowSelection();
				List<SingleScan> items = new ArrayList<SingleScan>();
				AbstractScanModel model = getModel();
				for (int row : rows) {
					items.add(model.getItem(row));
				}
				for (SingleScan scan : items) {
					model.deleteScan(scan);
				}
				table.redraw();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    batchHolder.setContent(emptyPart);
	    
	    table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				int[] rows = table.getRowSelection();
				if (rows.length > 1) {
					batchHolder.setContent(batchGroup);
					batchGroup.layout();
//					batchHolder.setMinSize(batchGroup.computeSize(xHint, yHint));
					batchHolder.getParent().layout();
					mainPart.layout();
				} else {
					batchHolder.setContent(emptyPart);
					emptyPart.layout();
					batchHolder.getParent().layout();
				}
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		final Composite runGroup = new Composite(this, SWT.NONE);
	    GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(5).applyTo(runGroup);
	    GridDataFactory.fillDefaults().grab(true, false).applyTo(runGroup);
	    
		final Label timeLabel = new Label(runGroup, SWT.NONE);
		timeLabel.setText("Time estimation");
		timeLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 40).applyTo(timeLabel);
		
		final Text timeText = new Text(runGroup, SWT.BORDER);
		timeText.setFont(Activator.getMiddleFont());
		timeText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).minSize(180, 40).applyTo(timeText);
		
		final Label finLabel = new Label(runGroup, SWT.NONE);
		finLabel.setText("Finish at");
		finLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 40).applyTo(finLabel);
		
		final Text finText = new Text(runGroup, SWT.BORDER);
		finText.setFont(Activator.getMiddleFont());
		finText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).minSize(180, 40).applyTo(finText);

	    
	    final Button runButton = new Button(runGroup, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setText("Run");
		runButton.setFont(Activator.getMiddleFont());
		runButton.setCursor(Activator.getHandCursor());
//		runButton.setSize(240, 64);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER).hint(240, 64).applyTo(runButton);
	}

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showInitScanPanel();
	}

	protected abstract AbstractScanModel getModel();
	
	@Override
	public void show() {
		AbstractScanModel model = getModel();
		if (model.getSize() == 0) {
			model.insertScan(0);
			table.redraw();
		}
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Full Experiment");
	}


}
