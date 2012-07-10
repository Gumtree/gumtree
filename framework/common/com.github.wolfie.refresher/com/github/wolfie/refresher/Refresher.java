package com.github.wolfie.refresher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.wolfie.refresher.client.ui.VRefresher;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * A Refresher is an non-visual component that polls the server for GUI updates.
 * <p/>
 * This makes asynchronous UI changes possible, that will be rendered even if
 * the user doesn't initiate a server-cycle explicitly.
 * 
 * @author Henrik Paul
 */
@com.vaadin.ui.ClientWidget(com.github.wolfie.refresher.client.ui.VRefresher.class)
public class Refresher extends AbstractComponent {
  
  public interface RefreshListener extends Serializable {
    public void refresh(Refresher source);
  }
  
  private static final long serialVersionUID = -2818447361687554688L;
  
  private static final long DEFAULT_REFRESH_INTERVAL = 1000;
  
  private final List<RefreshListener> refreshListeners = new ArrayList<RefreshListener>();
  
  private long refreshIntervalInMillis = DEFAULT_REFRESH_INTERVAL;
  
  /**
   * Creates a new {@link Refresher} instance, with a default refresh interval
   * of {@value Refresher#DEFAULT_REFRESH_INTERVAL}.
   */
  public Refresher() {
    super();
  }
  
  @Override
  public void paintContent(final PaintTarget target) throws PaintException {
    target.addAttribute("pollinginterval", refreshIntervalInMillis);
  }
  
  /**
   * Define a refresh interval.
   * 
   * @param intervalInMillis
   *          The desired refresh interval in milliseconds. An interval of zero
   *          or less temporarily inactivates the refresh.
   */
  public void setRefreshInterval(final long intervalInMillis) {
    refreshIntervalInMillis = intervalInMillis;
    requestRepaint();
  }
  
  /**
   * Get the currently used refreshing interval.
   * 
   * @return The refresh interval in milliseconds. A result of zero or less
   *         means that the refresher is currently inactive.
   */
  public long getRefreshInterval() {
    return refreshIntervalInMillis;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void changeVariables(final Object source,
      @SuppressWarnings("rawtypes") final Map variables) {
    super.changeVariables(source, variables);
    
    if (variables.containsKey(VRefresher.VARIABLE_REFRESH_EVENT)) {
      fireRefreshEvents();
    }
  }
  
  private void fireRefreshEvents() {
    for (final RefreshListener listener : refreshListeners) {
      listener.refresh(this);
    }
  }
  
  /**
   * Add a listener that will be triggered whenever this instance refreshes
   * itself
   * 
   * @param listener
   *          the listener
   * @return <code>true</code> if the adding was successful. <code>false</code>
   *         if the adding was unsuccessful, or <code>listener</code> is
   *         <code>null</code>.
   */
  public boolean addListener(final RefreshListener listener) {
    if (listener != null) {
      return refreshListeners.add(listener);
    } else {
      return false;
    }
  }
  
  /**
   * Removes a {@link RefreshListener} from this instance.
   * 
   * @param listener
   *          the listener to be removed.
   * @return <code>true</code> if removal was successful. A <code>false</code>
   *         most often means that <code>listener</code> wasn't added to this
   *         instance to begin with.
   */
  public boolean removeListener(final RefreshListener listener) {
    return refreshListeners.remove(listener);
  }
  
}
