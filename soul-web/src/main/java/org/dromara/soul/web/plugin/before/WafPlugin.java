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

package org.dromara.soul.web.plugin.before;

import cn.hutool.log.StaticLog;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.dto.convert.WafHandle;
import org.dromara.soul.common.dto.zk.RuleZkDTO;
import org.dromara.soul.common.dto.zk.SelectorZkDTO;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.PluginTypeEnum;
import org.dromara.soul.common.enums.WafEnum;
import org.dromara.soul.common.result.SoulResult;
import org.dromara.soul.common.utils.GsonUtils;
import org.dromara.soul.common.utils.JsonUtils;
import org.dromara.soul.web.cache.ZookeeperCacheManager;
import org.dromara.soul.web.plugin.AbstractSoulPlugin;
import org.dromara.soul.web.plugin.SoulPluginChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.doublespring.utils.U;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * use waf plugin we can control some access.
 *
 * @author xiaoyu(Myth)
 */
public class WafPlugin extends AbstractSoulPlugin {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WafPlugin.class);

    /**
     * Instantiates a new Waf plugin.
     *
     * @param zookeeperCacheManager the zookeeper cache manager
     */
    public WafPlugin(final ZookeeperCacheManager zookeeperCacheManager) {
        super(zookeeperCacheManager);
        StaticLog.debug("实例化WafPlugin", U.format(
                "zookeeperCacheManager", JSON.toJSON(zookeeperCacheManager)
        ));
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final SoulPluginChain chain, final SelectorZkDTO selector, final RuleZkDTO rule) {

        @NotBlank final String handle = rule.getHandle();

        final WafHandle wafHandle = GsonUtils.getInstance().fromJson(handle, WafHandle.class);

        if (Objects.isNull(wafHandle) || StringUtils.isBlank(wafHandle.getPermission())) {
            StaticLog.debug("waf handler can not config：{}", handle);
            Mono<Void> result = chain.execute(exchange);
            StaticLog.debug("执行WafPlugin -> WafHandle参数不合法", U.format("remoteAddress", exchange.getRequest().getRemoteAddress(), "SoulPluginChain", JSON.toJSON(chain), "result", JSON.toJSON(result)));
            return result;
        }
        // if reject
        if (WafEnum.REJECT.getName().equals(wafHandle.getPermission())) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            final SoulResult error = SoulResult.error(Integer.valueOf(wafHandle.getStatusCode()), Constants.REJECT_MSG);
            Mono<Void> result = exchange.getResponse().writeWith(
                    Mono.just(
                            exchange.getResponse().bufferFactory().wrap(Objects.requireNonNull(JsonUtils.toJson(error)).getBytes())
                    )
            );
            StaticLog.debug("执行WafPlugin -> 拒绝", U.format("remoteAddress", exchange.getRequest().getRemoteAddress(), "SoulPluginChain", JSON.toJSON(chain), "result", JSON.toJSON(result)));
            return result;
        }
        Mono<Void> result = chain.execute(exchange);
        StaticLog.debug("执行WafPlugin -> 放行", U.format("remoteAddress", exchange.getRequest().getRemoteAddress(), "SoulPluginChain", JSON.toJSON(chain), "result", JSON.toJSON(result)));
        return result;
    }

    /**
     * return plugin type.
     *
     * @return {@linkplain PluginTypeEnum}
     */
    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.BEFORE;
    }

    @Override
    public int getOrder() {
        return PluginEnum.WAF.getCode();
    }

    /**
     * acquire plugin name.
     *
     * @return plugin name.
     */
    @Override
    public String named() {
        return PluginEnum.WAF.getName();
    }
}
