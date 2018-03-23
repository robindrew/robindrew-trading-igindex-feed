package com.robindrew.trading.igindex.feed.igindex.session;

import com.robindrew.trading.provider.igindex.platform.IgSession;

public class SessionManager implements SessionManagerMBean {

	private final IgSession session;

	public SessionManager(IgSession session) {
		this.session = session;
	}

	@Override
	public String getEnvironment() {
		return session.getEnvironment().name();
	}

	@Override
	public String getUsername() {
		return session.getCredentials().getUsername();
	}

	@Override
	public String getApiKey() {
		return session.getCredentials().getApiKey();
	}

}
