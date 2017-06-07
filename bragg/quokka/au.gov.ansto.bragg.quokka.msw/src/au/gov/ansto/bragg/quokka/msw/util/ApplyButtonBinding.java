package au.gov.ansto.bragg.quokka.msw.util;

import java.util.Objects;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.observable.IProxyElementListener;
import org.gumtree.msw.ui.observable.ProxyElement;

// enable apply button when script has been modified
public class ApplyButtonBinding<TElement extends Element> implements IModelBinding {
	// fields
	private boolean enabled = false;
	// ui
	private final Text text;
	private final ModifyListener textModifyListener;
	private final IElementListener elementListener;
	private final IProxyElementListener<TElement> proxyListener;
	// deferred update
	private final Runnable updater;
	
	// construction
	public ApplyButtonBinding(final Text text, final Button button, final ProxyElement<TElement> proxy, final IDependencyProperty property) {
		this.text = text;
		
		updater = new Runnable() {
			@Override
			public void run() {
				if (!button.isDisposed())
					button.setEnabled(enabled);
			}
		};
		
		textModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				TElement element = proxy.getTarget();
				
				enabled =
						(element != null) &&
						!Objects.equals(text.getText(), element.get(property));
				
				text.getDisplay().asyncExec(updater);
			}
		};
		
		elementListener = new IElementListener() {
			@Override
			public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
				if (p == property) {
					enabled = !Objects.equals(text.getText(), newValue);
					text.getDisplay().asyncExec(updater);
				}
			}
			@Override
			public void onDisposed() {
				// ignore
			}
		};
		
		proxyListener = new IProxyElementListener<TElement>() {
			@Override
			public void onTargetChange(TElement oldTarget, TElement newTarget) {
				enabled =
						(newTarget != null) &&
						!Objects.equals(text.getText(), newTarget.get(property));
				
				text.getDisplay().asyncExec(updater);
			}
		};
		
		text.addModifyListener(textModifyListener);
		proxy.addListener(elementListener);
		proxy.addListener(proxyListener);
	}
	@Override
	public void dispose() {
		text.removeModifyListener(textModifyListener);
	}
}
