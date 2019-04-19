/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.soul.web.config;

import cn.hutool.log.StaticLog;
import com.alibaba.fastjson.JSON;
import org.dromara.soul.web.cache.UpstreamCacheManager;
import org.dromara.soul.web.cache.ZookeeperCacheManager;
import org.dromara.soul.web.disruptor.publisher.SoulEventPublisher;
import org.dromara.soul.web.filter.BodyWebFilter;
import org.dromara.soul.web.filter.ParamWebFilter;
import org.dromara.soul.web.filter.TimeWebFilter;
import org.dromara.soul.web.handler.SoulHandlerMapping;
import org.dromara.soul.web.handler.SoulWebHandler;
import org.dromara.soul.web.plugin.SoulPlugin;
import org.dromara.soul.web.plugin.after.MonitorPlugin;
import org.dromara.soul.web.plugin.after.ResponsePlugin;
import org.dromara.soul.web.plugin.before.GlobalPlugin;
import org.dromara.soul.web.plugin.before.SignPlugin;
import org.dromara.soul.web.plugin.before.WafPlugin;
import org.dromara.soul.web.plugin.dubbo.GenericParamService;
import org.dromara.soul.web.plugin.dubbo.GenericParamServiceImpl;
import org.dromara.soul.web.plugin.function.DividePlugin;
import org.dromara.soul.web.plugin.function.RateLimiterPlugin;
import org.dromara.soul.web.plugin.function.RewritePlugin;
import org.dromara.soul.web.plugin.ratelimter.RedisRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import top.doublespring.utils.U;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SoulConfiguration.
 *
 * @author xiaoyu(Myth)
 */
@Configuration
@ComponentScan("org.dromara.soul")
public class SoulConfiguration {

    private final ZookeeperCacheManager zookeeperCacheManager;

    private final SoulEventPublisher soulEventPublisher;

    private final RedisRateLimiter redisRateLimiter;

    private final UpstreamCacheManager upstreamCacheManager;

    /**
     * Instantiates a new Soul configuration.
     *
     * @param zookeeperCacheManager the zookeeper cache manager
     * @param soulEventPublisher    the soul event publisher
     * @param redisRateLimiter      the redis rate limiter
     * @param upstreamCacheManager  the upstream cache manager
     */
    @Autowired(required = false)
    public SoulConfiguration(final ZookeeperCacheManager zookeeperCacheManager,
                             final SoulEventPublisher soulEventPublisher,
                             final RedisRateLimiter redisRateLimiter,
                             final UpstreamCacheManager upstreamCacheManager) {
        StaticLog.debug("初始化SoulConfiguration", U.format(
                "zookeeperCacheManager", JSON.toJSON(zookeeperCacheManager),
                "soulEventPublisher", JSON.toJSON(soulEventPublisher),
                "redisRateLimiter", JSON.toJSON(redisRateLimiter),
                "upstreamCacheManager", JSON.toJSON(upstreamCacheManager)
        ));
        this.zookeeperCacheManager = zookeeperCacheManager;
        this.soulEventPublisher = soulEventPublisher;
        this.redisRateLimiter = redisRateLimiter;
        this.upstreamCacheManager = upstreamCacheManager;
    }

    /**
     * init global plugin.
     *
     * @return {@linkplain GlobalPlugin}
     */
    @Bean
    public SoulPlugin globalPlugin() {
        StaticLog.debug("实例化GlobalPlugin");
        return new GlobalPlugin();
    }


    /**
     * init sign plugin.
     *
     * @return {@linkplain SignPlugin}
     */
    @Bean
    public SoulPlugin signPlugin() {
        StaticLog.debug("实例化SignPlugin");
        return new SignPlugin(zookeeperCacheManager);
    }

    /**
     * init waf plugin.
     *
     * @return {@linkplain WafPlugin}
     */
    @Bean
    public SoulPlugin wafPlugin() {
        StaticLog.debug("实例化WafPlugin");
        return new WafPlugin(zookeeperCacheManager);
    }

    /**
     * init monitor plugin.
     *
     * @return {@linkplain MonitorPlugin}
     */
    @Bean
    public SoulPlugin monitorPlugin() {
        StaticLog.debug("实例化MonitorPlugin");
        return new MonitorPlugin(soulEventPublisher, zookeeperCacheManager);
    }

    /**
     * init rateLimiterPlugin.
     *
     * @return {@linkplain RateLimiterPlugin}
     */
    @Bean
    public SoulPlugin rateLimiterPlugin() {
        StaticLog.debug("实例化RateLimiterPlugin");
        return new RateLimiterPlugin(zookeeperCacheManager, redisRateLimiter);
    }

    /**
     * init rewritePlugin.
     *
     * @return {@linkplain RewritePlugin}
     */
    @Bean
    public SoulPlugin rewritePlugin() {
        StaticLog.debug("实例化RewritePlugin");
        return new RewritePlugin(zookeeperCacheManager);
    }

    /**
     * init dividePlugin.
     *
     * @return {@linkplain DividePlugin}
     */
    @Bean
    public SoulPlugin dividePlugin() {
        StaticLog.debug("实例化DividePlugin");
        return new DividePlugin(zookeeperCacheManager, upstreamCacheManager);
    }

    /**
     * init responsePlugin.
     *
     * @return {@linkplain ResponsePlugin}
     */
    @Bean
    public SoulPlugin responsePlugin() {
        StaticLog.debug("实例化ResponsePlugin");
        return new ResponsePlugin();
    }

    /**
     * init SoulWebHandler.
     *
     * @param plugins this plugins is All impl SoulPlugin.
     * @return {@linkplain SoulWebHandler}
     */
    @Bean
    public SoulWebHandler soulWebHandler(final List<SoulPlugin> plugins) {
        StaticLog.debug("实例化SoulWebHandler");
        final List<SoulPlugin> soulPlugins = plugins.stream()
                .sorted((m, n) -> {
                    if (m.pluginType().equals(n.pluginType())) {
                        return m.getOrder() - n.getOrder();
                    } else {
                        return m.pluginType().getName().compareTo(n.pluginType().getName());
                    }
                }).collect(Collectors.toList());
        return new SoulWebHandler(soulPlugins);
    }

    /**
     * init  SoulHandlerMapping.
     *
     * @param soulWebHandler {@linkplain SoulWebHandler}
     * @return {@linkplain SoulHandlerMapping}
     */
    @Bean
    public SoulHandlerMapping soulHandlerMapping(final SoulWebHandler soulWebHandler) {
        StaticLog.debug("实例化SoulHandlerMapping");
        return new SoulHandlerMapping(soulWebHandler);
    }

    /**
     * Body web filter web filter.
     *
     * @return the web filter
     */
    @Bean
    @Order(-1)
    public WebFilter bodyWebFilter() {
        StaticLog.debug("实例化BodyWebFilter");
        return new BodyWebFilter();
    }

    /**
     * init param web filter.
     *
     * @return {@linkplain ParamWebFilter}
     */
    @Bean
    @Order(1)
    public WebFilter paramWebFilter() {
        StaticLog.debug("实例化ParamWebFilter");
        return new ParamWebFilter();
    }

    /**
     * init time web filter.
     *
     * @return {@linkplain TimeWebFilter}
     */
    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "soul.timeVerify.enabled", matchIfMissing = true)
    public WebFilter timeWebFilter() {
        StaticLog.debug("实例化TimeWebFilter");
        return new TimeWebFilter();
    }

    /**
     * Generic param service generic param service.
     *
     * @return the generic param service
     */
    @Bean
    @ConditionalOnMissingBean(value = GenericParamService.class, search = SearchStrategy.ALL)
    public GenericParamService genericParamService() {
        StaticLog.debug("实例化GenericParamService");
        return new GenericParamServiceImpl();
    }
}
