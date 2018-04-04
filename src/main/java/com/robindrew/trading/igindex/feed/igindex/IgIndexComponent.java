package com.robindrew.trading.igindex.feed.igindex;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;
import static com.robindrew.common.dependency.DependencyFactory.setDependency;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.mbean.IMBeanRegistry;
import com.robindrew.common.mbean.annotated.AnnotatedMBeanRegistry;
import com.robindrew.common.properties.map.type.EnumProperty;
import com.robindrew.common.properties.map.type.IProperty;
import com.robindrew.common.properties.map.type.StringProperty;
import com.robindrew.common.service.component.AbstractIdleComponent;
import com.robindrew.trading.IInstrument;
import com.robindrew.trading.igindex.feed.igindex.connection.ConnectionManager;
import com.robindrew.trading.igindex.feed.igindex.connection.IConnectionManager;
import com.robindrew.trading.igindex.feed.igindex.session.SessionManager;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IStreamingService;
import com.robindrew.trading.price.precision.PricePrecision;
import com.robindrew.trading.price.tick.io.stream.sink.PriceTickFileSink;
import com.robindrew.trading.provider.igindex.IgInstrument;
import com.robindrew.trading.provider.igindex.platform.IIgSession;
import com.robindrew.trading.provider.igindex.platform.IgCredentials;
import com.robindrew.trading.provider.igindex.platform.IgEnvironment;
import com.robindrew.trading.provider.igindex.platform.IgSession;
import com.robindrew.trading.provider.igindex.platform.IgTradingPlatform;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.IgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.IMarketNavigationCache;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarketnavigation.MarketNavigationCache;
import com.robindrew.trading.provider.igindex.platform.streaming.IgStreamingServiceMonitor;
import com.robindrew.trading.provider.igindex.platform.streaming.subscription.charttick.ChartTickPriceStream;

public class IgIndexComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(IgIndexComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("igindex.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("igindex.username");
	private static final IProperty<String> propertyPassword = new StringProperty("igindex.password");
	private static final IProperty<IgEnvironment> propertyEnvironment = new EnumProperty<>(IgEnvironment.class, "igindex.environment");

	private volatile IgStreamingServiceMonitor monitor;

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();
		IgEnvironment environment = propertyEnvironment.get();

		IgCredentials credentials = new IgCredentials(apiKey, username, password);

		log.info("Creating Session", environment);
		log.info("Environment: {}", environment);
		log.info("User: {}", credentials.getUsername());
		IgSession session = new IgSession(credentials, environment);
		setDependency(IIgSession.class, session);

		log.info("Creating Account Manager");
		SessionManager sessionManager = new SessionManager(session);
		registry.register(sessionManager);

		log.info("Creating REST Service");
		IMarketNavigationCache marketNavigationCache = new MarketNavigationCache();
		IIgRestService rest = new IgRestService(session, marketNavigationCache);
		setDependency(IIgRestService.class, rest);
		setDependency(IMarketNavigationCache.class, marketNavigationCache);

		log.info("Creating Trading Platform");
		IgTradingPlatform platform = new IgTradingPlatform(rest);
		setDependency(ITradingPlatform.class, platform);

		log.info("Creating Connection manager");
		IConnectionManager connectionManager = new ConnectionManager(rest, platform);
		registry.register(connectionManager);
		setDependency(IConnectionManager.class, connectionManager);

		log.info("Logging in ...");
		connectionManager.login();

		log.info("Subscribing ...");
		createStreamingSubscriptions();

		log.info("Creating Streaming Service Monitor");
		monitor = new IgStreamingServiceMonitor(platform);
		monitor.start();
	}

	public IgStreamingServiceMonitor getMonitor() {
		return monitor;
	}

	private void createStreamingSubscriptions() {
		// createStreamingSubscription(IgInstrument.SUNDAY_DOW_JONES, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SUNDAY_FTSE_100, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SUNDAY_DAX, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SPOT_BITCOIN, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SPOT_ETHER, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SPOT_RIPPLE, new PricePrecision(2, 900, 90000));
		// createStreamingSubscription(IgInstrument.SPOT_LITECOIN, new PricePrecision(2, 900, 90000));

		// Currencies
		createStreamingSubscription(IgInstrument.SPOT_AUD_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_EUR_JPY, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_EUR_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_GBP_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_USD_CHF, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_USD_JPY, new PricePrecision(2, 900, 90000));

		// Indices
		createStreamingSubscription(IgInstrument.WEEKDAY_FTSE_100, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.WEEKDAY_DOW_JONES, new PricePrecision(2, 900, 90000));

		// Commodities
		createStreamingSubscription(IgInstrument.SPOT_SILVER, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_GOLD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_US_CRUDE, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(IgInstrument.SPOT_BRENT_CRUDE, new PricePrecision(2, 900, 90000));

	}

	private void createStreamingSubscription(IInstrument instrument, PricePrecision precision) {
		ITradingPlatform platform = getDependency(ITradingPlatform.class);

		// Create the underlying stream
		ChartTickPriceStream priceStream = new ChartTickPriceStream(instrument, precision);
		priceStream.start();

		// Create the output file
		PriceTickFileSink priceFileSink = new PriceTickFileSink(instrument, new File("c:/temp/prices/igindex"));
		priceFileSink.start();

		// Register the stream to make it available through the platform
		IStreamingService streamingService = platform.getStreamingService();
		streamingService.register(priceStream);

		// Register all the sinks
		priceStream.register(priceFileSink);
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}
