package org.gumtree.gumnix.sics.internal.ui.login;

public interface ILoginHandler {

	public void login(final boolean forced);

	public void setNoMoreLogin(boolean noMoreLogin);

}
