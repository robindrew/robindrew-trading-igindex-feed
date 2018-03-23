package com.robindrew.trading.igindex.feed.igindex.connection;

import java.util.List;

import com.robindrew.common.mbean.annotated.Name;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getaccounts.Account;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.MarketNavigation;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarkets.Markets;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getpositions.MarketPosition;
import com.robindrew.trading.provider.igindex.platform.rest.executor.login.LoginDetails;

public interface ConnectionManagerMBean {

	boolean isLoggedIn();

	boolean login();

	boolean logout();

	LoginDetails getLoginDetails();

	List<Account> listAccounts();

	List<MarketPosition> listPositions();

	Markets getMarkets(@Name("epic") String instrument);

	MarketNavigation listMarkets(@Name("id") int id, @Name("latest") boolean latest);

}
