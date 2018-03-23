package com.robindrew.trading.igindex.feed;

import com.robindrew.common.service.AbstractService;
import com.robindrew.common.service.component.heartbeat.HeartbeatComponent;
import com.robindrew.common.service.component.logging.LoggingComponent;
import com.robindrew.common.service.component.properties.PropertiesComponent;
import com.robindrew.common.service.component.stats.StatsComponent;
import com.robindrew.trading.igindex.feed.igindex.IgIndexComponent;
import com.robindrew.trading.igindex.feed.jetty.JettyComponent;

public class IgIndexFeedService extends AbstractService {

	/**
	 * Entry point for the TurnEngine Admin Client Service.
	 */
	public static void main(String[] args) {
		IgIndexFeedService service = new IgIndexFeedService(args);
		service.startAsync();
	}

	private final JettyComponent jetty = new JettyComponent();
	private final HeartbeatComponent heartbeat = new HeartbeatComponent();
	private final PropertiesComponent properties = new PropertiesComponent();
	private final LoggingComponent logging = new LoggingComponent();
	private final StatsComponent stats = new StatsComponent();
	private final IgIndexComponent igindex = new IgIndexComponent();

	public IgIndexFeedService(String[] args) {
		super(args);
	}

	@Override
	protected void startupService() throws Exception {
		start(properties);
		start(logging);
		start(heartbeat);
		start(stats);
		start(jetty);
		start(igindex);
	}

	@Override
	protected void shutdownService() throws Exception {
		stop(jetty);
		stop(heartbeat);
	}
}
