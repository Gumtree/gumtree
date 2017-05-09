package org.gumtree.msw.dummy;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.IModelListener;
import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.IModelProxyFilter;
import org.gumtree.msw.INotificationLock;
import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.model.DataSource;

public class DummyModelProxy implements IModelProxy {
	// fields
	private IModel model;
	private IRefIdProvider idProvider;
	private List<IModelProxyFilter> filters;
	// notification chain
	private int notificationSuspendCount;
	private final Queue<Runnable> notifications;
	private final List<IModelListener> listeners;
	
	// construction
	public DummyModelProxy(IModel model) {
		this.model = model;
		this.idProvider = model.getIdProvider();
		
		filters = new ArrayList<>();
		
		notificationSuspendCount = 0;
		notifications = new LinkedList<>();
		listeners = new ArrayList<>();
		
		model.addListener(new ModelListener());
		model.gainControl(idProvider.getSourceId());
	}
	
	// methods
	@Override
	public IRefIdProvider getIdProvider() {
		return idProvider;
	}
	@Override
	public DataSource getXsd() {
		return model.getXsd();
	}
	@Override
	public boolean isConnected() {
		return true;
	}
	@Override
	public boolean hasControl() {
		return true;
	}
	@Override
	public Object getProperty(Iterable<String> elementPath, String property) {
		return model.getProperty(elementPath, property);
	}
	@Override
	public boolean validateProperty(Iterable<String> elementPath, String property, Object newValue) {
		for (IModelProxyFilter filter : filters)
			if (!filter.validateProperty(elementPath, property, newValue))
				return false;
		
		return model.validateProperty(elementPath, property, newValue);
	}
	@Override
	public Iterable<String> getListElements(Iterable<String> listPath) {
		return model.getListElements(listPath);
	}

	// communication
	@Override
	public void command(ICommand command) {
		for (IModelProxyFilter filter : filters)
			if (!filter.command(command))
				return;
		
		model.command(command);
	}
	// filters
	@Override
	public synchronized void addFilter(IModelProxyFilter filter) {
		if (filters.contains(filter))
			throw new Error("filter already exists");
		
		filters.add(filter);
	}
	@Override
	public synchronized boolean removeFilter(IModelProxyFilter filter) {
		return filters.remove(filter);
	}

	// serialization
	@Override
	public void reset() {
		model.reset();
	}
	@Override
	public boolean serializeTo(Iterable<String> elementPath, OutputStream stream) {
		return model.serializeTo(elementPath, stream);
	}
	@Override
	public boolean deserializeFrom(InputStream stream) {
		return model.deserializeFrom(stream);
	}
	
	// listeners
	@Override
	public synchronized void addListener(IModelListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);	
	}
	@Override
	public synchronized boolean removeListener(IModelListener listener) {
		return listeners.remove(listener);
	}
	// multithread support
	@Override
	public INotificationLock suspendNotifications() {
		return new NotificationLock();
	}
	// helpers
	private synchronized void incrementSuspendCount() {
		++notificationSuspendCount;
	}
	private synchronized void decrementSuspendCount() {
		if (0 == --notificationSuspendCount)
			while (!notifications.isEmpty())
				notifications.poll().run();
	}
	private synchronized void enqueue(Runnable notification) {
		if (0 == notificationSuspendCount)
			notification.run();
		else
			notifications.add(notification);
	}
	
	// notification lock
	private class NotificationLock implements INotificationLock {
		// fields
		private boolean closed;
		
		// construction
		public NotificationLock() {
			incrementSuspendCount();
			closed = false;
		}

		// methods
		@Override
		public void close() {
			if (!closed) {
				closed = true;
				decrementSuspendCount();
			}
		}
	}

	// notification buffering
	private class ModelListener implements IModelListener {
		// control
		@Override
		public void onGainedControl(final int clientId) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onGainedControl(clientId);
				}
			});
		}
		@Override
		public void onReleasedControl(final int clientId) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onReleasedControl(clientId);
				}
			});
		}

		// content
		@Override
		public void onReset() {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onReset();
				}
			});
		}
		// properties
		@Override
		public void onChangedProperty(final Iterable<String> elementPath, final String property, final Object oldValue, final Object newValue) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onChangedProperty(elementPath, property, oldValue, newValue);
				}
			});
		}
		// list elements
		@Override
		public void onAddedListElement(final Iterable<String> listPath, final String elementName) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onAddedListElement(listPath, elementName);
				}
			});
		}
		@Override
		public void onDeletedListElement(final Iterable<String> listPath, final String elementName) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onDeletedListElement(listPath, elementName);
				}
			});
		}
		@Override
		public void onRecoveredListElement(final Iterable<String> listPath, final String elementName) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onRecoveredListElement(listPath, elementName);
				}
			});
		}

		// experiment
		@Override
		public void onStartedExperiment() {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onStartedExperiment();
				}
			});
		}
		@Override
		public void onPausedExperiment() {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onPausedExperiment();
				}
			});
		}
		@Override
		public void onStoppedExperiment() {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onStoppedExperiment();
				}
			});
		}

		// command
		@Override
		public void onCommandSucceeded(final ICommand command, final ICommand undoCommand) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onCommandSucceeded(command, undoCommand);
				}
			});
		}
		@Override
		public void onCommandFailed(final ICommand command) {
			enqueue(new Runnable() {
				@Override
				public void run() {
					for (IModelListener listener : listeners)
						listener.onCommandFailed(command);
				}
			});
		}
	}
}
