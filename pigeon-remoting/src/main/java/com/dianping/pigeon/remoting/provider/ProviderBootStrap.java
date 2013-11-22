/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.registry.config.RegistryConfigLoader;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ShutdownHookListener;
import com.dianping.pigeon.remoting.provider.process.RequestProcessHandlerFactory;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;
import com.dianping.pigeon.util.NetUtils;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderBootStrap {

	private static Logger logger = LoggerLoader.getLogger(ServiceProviderFactory.class);
	static volatile Server server = null;

	public static Server startup(ProviderConfig providerConfig) {
		if (server == null) {
			synchronized (ProviderBootStrap.class) {
				if (server == null) {
					int availablePort = NetUtils.getAvailablePort(providerConfig.getPort());
					providerConfig.setPort(availablePort);
					RegistryConfigLoader.init();
					RequestProcessHandlerFactory.init();
					server = ExtensionLoader.getExtension(ServerFactory.class).createServer(providerConfig);
					if (server != null) {
						server.start();
						Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookListener(server)));
						if (logger.isInfoEnabled()) {
							logger.info("pigeon server[version:" + VersionUtils.VERSION + "] has been started at port:"
									+ server.getPort());
						}
					}
				}
			}
		}
		return server;
	}

	public static void shutdown() {
		RequestProcessHandlerFactory.clearServerInternalFilters();
		synchronized (ProviderBootStrap.class) {
			if (server != null) {
				server.stop();
				if (logger.isInfoEnabled()) {
					logger.info("pigeon server[version:" + VersionUtils.VERSION + "] has been shutdown");
				}
			}
		}
	}

}