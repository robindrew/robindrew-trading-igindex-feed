package com.robindrew.trading.igindex.feed.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;
import static java.lang.System.currentTimeMillis;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.common.text.Strings;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.IStreamingService;
import com.robindrew.trading.provider.igindex.platform.IIgSession;
import com.robindrew.trading.provider.igindex.platform.rest.IIgRestService;
import com.robindrew.trading.provider.igindex.platform.rest.executor.getmarkets.Markets;

public class FeedsPage extends AbstractServicePage {

	public FeedsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

		IIgSession session = getDependency(IIgSession.class);
		dataMap.put("user", session.getCredentials().getUsername());
		dataMap.put("environment", session.getEnvironment());

		ITradingPlatform platform = getDependency(ITradingPlatform.class);
		IStreamingService service = platform.getStreamingService();
		dataMap.put("feeds", getFeeds(service.getPriceStreams()));
	}

	private Set<Feed> getFeeds(Set<IInstrumentPriceStream> subscriptions) {
		IIgRestService rest = getDependency(IIgRestService.class);
		Set<Feed> feeds = new TreeSet<>();
		for (IInstrumentPriceStream subscription : subscriptions) {
			String epic = subscription.getInstrument().getName();
			Markets markets = rest.getMarkets(epic, false);
			feeds.add(new Feed(subscription, markets));
		}
		return feeds;
	}

	public static class Feed implements Comparable<Feed> {

		private final IInstrumentPriceStream subscription;
		private final Markets markets;

		public Feed(IInstrumentPriceStream subscription, Markets markets) {
			this.subscription = subscription;
			this.markets = markets;
		}

		public IInstrumentPriceStream getSubscription() {
			return subscription;
		}

		public Markets getMarkets() {
			return markets;
		}

		public String getTimeSinceLastUpdate() {
			long time = subscription.getLatestPrice().getUpdateTime();
			return Strings.duration(time, currentTimeMillis());
		}

		@Override
		public int compareTo(Feed that) {
			return subscription.getListener().getInstrument().compareTo(that.subscription.getListener().getInstrument());
		}
	}
}
