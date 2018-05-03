package com.robindrew.trading.igindex.feed.igindex.connection;

import java.util.List;

import com.robindrew.common.mbean.annotated.Name;
import com.robindrew.trading.igindex.platform.rest.executor.getaccounts.response.Account;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.response.MarketNavigation;
import com.robindrew.trading.igindex.platform.rest.executor.getmarkets.response.Markets;
import com.robindrew.trading.igindex.platform.rest.executor.getpositions.MarketPosition;
import com.robindrew.trading.igindex.platform.rest.executor.login.LoginResponse;

public interface ConnectionManagerMBean {

	boolean isLoggedIn();

	boolean login();

	boolean logout();

	LoginResponse getLoginDetails();

	List<Account> listAccounts();

	List<MarketPosition> listPositions();

	Markets getMarkets(@Name("epic") String instrument);

	MarketNavigation listMarkets(@Name("id") int id, @Name("latest") boolean latest);

}
