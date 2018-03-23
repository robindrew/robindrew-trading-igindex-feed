package com.robindrew.trading.igindex.feed.igindex.connection;

import static com.robindrew.common.dependency.DependencyFactory.clearDependency;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.util.Check;
import com.robindrew.trading.platform.streaming.IStreamingService;
import com.robindrew.trading.provider.igindex.platform.IIgTradingPlatform;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getaccounts.Account;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.MarketNavigation;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarkets.Markets;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getpositions.MarketPosition;
import com.robindrew.trading.provider.igindex.platform.rest.executor.login.LoginDetails;

public class ConnectionManager implements IConnectionManager, ConnectionManagerMBean {

	private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

	private final IIgRestService rest;
	private final IIgTradingPlatform platform;
	private volatile LoginDetails details;

	public ConnectionManager(IIgRestService rest, IIgTradingPlatform platform) {
		this.rest = Check.notNull("rest", rest);
		this.platform = Check.notNull("platform", platform);
	}

	@Override
	public boolean isLoggedIn() {
		return details != null;
	}

	@Override
	public LoginDetails getLoginDetails() {
		if (details == null) {
			throw new IllegalStateException("Not logged in");
		}
		return details;
	}

	@Override
	public List<Account> listAccounts() {
		return rest.getAccountList();
	}

	@Override
	public MarketNavigation listMarkets(int id, boolean latest) {
		return rest.getMarketNavigation(id, latest);
	}

	@Override
	public List<MarketPosition> listPositions() {
		return rest.getPositionList();
	}

	@Override
	public Markets getMarkets(String epic) {
		return rest.getMarkets(epic, true);
	}

	@Override
	public boolean login() {
		try {
			details = rest.login();

			log.info("Registering Subscriptions");
			IStreamingService service = platform.getStreamingService();
			service.connect();
			return true;

		} catch (Exception e) {
			log.warn("Login Failed", e);
			return false;
		}
	}

	@Override
	public boolean logout() {
		try {
			details = null;
			clearDependency(IIgTradingPlatform.class);
			rest.logout();
			return true;
		} catch (Exception e) {
			log.warn("Login Failed", e);
			return false;
		}
	}

}
