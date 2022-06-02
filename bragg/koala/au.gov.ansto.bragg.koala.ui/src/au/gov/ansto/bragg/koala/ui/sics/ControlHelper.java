package au.gov.ansto.bragg.koala.ui.sics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants.ControllerState;

public class ControlHelper {

	private ISicsProxy sicsProxy;
	private final static Color BUSY_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final static Color IDLE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	
	public ControlHelper() {
		
	}

	private static ISicsProxy getProxy() {
		return SicsManager.getSicsProxy();
	}
	
	private static ISicsModel getModel() {
		return SicsManager.getSicsModel();
	}
	
	private static ControlHelper instance;
	
	public static synchronized ControlHelper getInstance() {
		if (instance == null) {
			instance = new ControlHelper();
		}
		return instance;
	}
	
	public void observePath(final String path, final Label currentControl, final Text targetControl) {
		final ISicsControllerListener listener = new ControllerListener(currentControl, targetControl);
		final ISicsController controller = SicsManager.getSicsModel().findControllerByPath(path);
		if (controller != null) {
			controller.addControllerListener(listener);
		}
		targetControl.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				controller.removeControllerListener(listener);
			}
		});
		getProxy().addProxyListener(new SicsProxyListenerAdapter() {
			
			@Override
			public void connect() {
				if (controller instanceof IDynamicController) {
					try {
						Object value = ((IDynamicController) controller).getValue();
						currentControl.setText(String.valueOf(value));
					} catch (SicsModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void addProxyListener(ISicsProxyListener listener) {
		getProxy().addProxyListener(listener);
	}
	
	public void removeProxyListener(ISicsProxyListener listener) {
		getProxy().removeProxyListener(listener);
	}
	
	public boolean isConnected() {
		return getProxy().isConnected();
	}
	
	class ControllerListener implements ISicsControllerListener {

		private Label currentControl;
		private Text targetControl;
		private Object currentValue;
		
		public ControllerListener(Label current, Text target) {
			this.currentControl = current;
			this.targetControl = target;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getCurrent().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						currentControl.setForeground(BUSY_COLOR);
					} else {
						currentControl.setForeground(IDLE_COLOR);
					}
				}
			});
			
		}
		
		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			if (newValue != null && !newValue.toString().equals(currentValue)) {
				currentValue = newValue.toString();
				Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						currentControl.setText(String.valueOf(newValue));
					}
				});
			}
		}
		@Override
		public void updateEnabled(boolean isEnabled) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void updateTarget(final Object oldValue, final Object newValue) {
			if (newValue != null) {
				Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						targetControl.setText(String.valueOf(newValue));
					}
				});
			}
		}
	}
	
	public static String syncExec(String command) throws SicsException {
		return getProxy().syncRun(command);
	}
}
