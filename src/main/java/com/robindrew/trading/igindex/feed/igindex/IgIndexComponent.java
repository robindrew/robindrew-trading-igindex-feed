package com.robindrew.trading.igindex.feed.igindex;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;
import static com.robindrew.common.dependency.DependencyFactory.setDependency;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.mbean.IMBeanRegistry;
import com.robindrew.common.mbean.annotated.AnnotatedMBeanRegistry;
import com.robindrew.common.properties.map.type.EnumProperty;
import com.robindrew.common.properties.map.type.FileProperty;
import com.robindrew.common.properties.map.type.IProperty;
import com.robindrew.common.properties.map.type.StringProperty;
import com.robindrew.common.service.component.AbstractIdleComponent;
import com.robindrew.trading.igindex.IIgIndexInstrument;
import com.robindrew.trading.igindex.IgIndexInstrument;
import com.robindrew.trading.igindex.feed.igindex.connection.ConnectionManager;
import com.robindrew.trading.igindex.feed.igindex.connection.IConnectionManager;
import com.robindrew.trading.igindex.feed.igindex.session.SessionManager;
import com.robindrew.trading.igindex.platform.IIgIndexSession;
import com.robindrew.trading.igindex.platform.IIgIndexTradingPlatform;
import com.robindrew.trading.igindex.platform.IgIndexCredentials;
import com.robindrew.trading.igindex.platform.IgIndexEnvironment;
import com.robindrew.trading.igindex.platform.IgIndexSession;
import com.robindrew.trading.igindex.platform.IgIndexTradingPlatform;
import com.robindrew.trading.igindex.platform.rest.IIgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.IgIndexRestService;
import com.robindrew.trading.igindex.platform.rest.executor.getmarketnavigation.cache.IMarketNavigationCache;
import com.robindrew.trading.igindex.platform.streaming.IgIndexStreamingServiceMonitor;
import com.robindrew.trading.log.FileBackedTransactionLog;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.IStreamingService;
import com.robindrew.trading.price.candle.io.stream.sink.PriceCandleFileSink;
import com.robindrew.trading.price.precision.PricePrecision;

public class IgIndexComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(IgIndexComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("igindex.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("igindex.username");
	private static final IProperty<String> propertyPassword = new StringProperty("igindex.password");
	private static final IProperty<IgIndexEnvironment> propertyEnvironment = new EnumProperty<>(IgIndexEnvironment.class, "igindex.environment");
	private static final IProperty<String> propertyTickOutputDir = new StringProperty("tick.output.dir");
	private static final IProperty<File> propertyTransactionLogDir = new FileProperty("transaction.log.dir");

	private volatile IgIndexStreamingServiceMonitor monitor;

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();
		IgIndexEnvironment environment = propertyEnvironment.get();
		File transactionLogDir = propertyTransactionLogDir.get();

		IgIndexCredentials credentials = new IgIndexCredentials(apiKey, username, password);

		log.info("Creating Session", environment);
		log.info("Environment: {}", environment);
		log.info("User: {}", credentials.getUsername());
		IgIndexSession session = new IgIndexSession(credentials, environment);
		setDependency(IIgIndexSession.class, session);

		log.info("Creating Account Manager");
		SessionManager sessionManager = new SessionManager(session);
		registry.register(sessionManager);

		log.info("Creating Transaction Log");
		FileBackedTransactionLog transactionLog = new FileBackedTransactionLog(transactionLogDir);
		transactionLog.start("IgIndexTransactionLog");

		log.info("Creating REST Service");
		IgIndexRestService rest = new IgIndexRestService(session, transactionLog);
		setDependency(IIgIndexRestService.class, rest);
		setDependency(IMarketNavigationCache.class, rest.getMarketNavigationCache());

		log.info("Creating Trading Platform");
		IIgIndexTradingPlatform platform = new IgIndexTradingPlatform(rest);
		setDependency(IIgIndexTradingPlatform.class, platform);

		log.info("Creating Connection manager");
		IConnectionManager connectionManager = new ConnectionManager(rest, platform);
		registry.register(connectionManager);
		setDependency(IConnectionManager.class, connectionManager);

		log.info("Logging in ...");
		connectionManager.login();

		log.info("Subscribing ...");
		createStreamingSubscriptions();

		log.info("Creating Streaming Service Monitor");
		monitor = new IgIndexStreamingServiceMonitor(platform);
		monitor.start();
	}

	public IgIndexStreamingServiceMonitor getMonitor() {
		return monitor;
	}

	private void createStreamingSubscriptions() {
		// createStreamingSubscription(IgInstrument.SUNDAY_DOW_JONES, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SUNDAY_FTSE_100, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SUNDAY_DAX, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SPOT_BITCOIN, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SPOT_ETHER, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SPOT_RIPPLE, new PricePrecision(2));
		// createStreamingSubscription(IgInstrument.SPOT_LITECOIN, new PricePrecision(2));

		// Currencies
		createStreamingSubscription(IgIndexInstrument.SPOT_AUD_USD, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_EUR_JPY, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_EUR_USD, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_GBP_USD, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_USD_CHF, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_USD_JPY, new PricePrecision(2));

		// Indices
		createStreamingSubscription(IgIndexInstrument.WEEKDAY_FTSE_100, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.WEEKDAY_DOW_JONES, new PricePrecision(2));

		// Commodities
		createStreamingSubscription(IgIndexInstrument.SPOT_XAG_USD, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_XAU_USD, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_US_CRUDE, new PricePrecision(2));
		createStreamingSubscription(IgIndexInstrument.SPOT_BRENT_CRUDE, new PricePrecision(2));

	}

	private void createStreamingSubscription(IIgIndexInstrument instrument, PricePrecision precision) {
		ITradingPlatform<IIgIndexInstrument> platform = getDependency(ITradingPlatform.class);

		// Register the stream to make it available through the platform
		IStreamingService<IIgIndexInstrument> streaming = platform.getStreamingService();
		streaming.subscribeToPrices(instrument);
		IInstrumentPriceStream<IIgIndexInstrument> priceStream = streaming.getPriceStream(instrument);

		// Create the output file
		PriceCandleFileSink priceFileSink = new PriceCandleFileSink(instrument, new File(propertyTickOutputDir.get()));
		priceFileSink.start();
		priceStream.register(priceFileSink);
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}
