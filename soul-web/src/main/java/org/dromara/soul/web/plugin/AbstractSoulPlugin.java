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

package org.dromara.soul.web.plugin;

import cn.hutool.log.StaticLog;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.dto.zk.PluginZkDTO;
import org.dromara.soul.common.dto.zk.RuleZkDTO;
import org.dromara.soul.common.dto.zk.SelectorZkDTO;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.PluginTypeEnum;
import org.dromara.soul.common.enums.SelectorTypeEnum;
import org.dromara.soul.common.result.SoulResult;
import org.dromara.soul.common.utils.JsonUtils;
import org.dromara.soul.common.utils.LogUtils;
import org.dromara.soul.web.cache.ZookeeperCacheManager;
import org.dromara.soul.web.condition.strategy.MatchStrategyFactory;
import org.dromara.soul.web.request.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.doublespring.utils.U;

import java.util.List;
import java.util.Objects;


/**
 * abstract soul plugin please extends.
 *
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
public abstract class AbstractSoulPlugin implements SoulPlugin {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSoulPlugin.class);

    private final ZookeeperCacheManager zookeeperCacheManager;

    /**
     * Process the Web request and (optionally) delegate to the next
     * {@code WebFilter} through the given {@link SoulPluginChain}.
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final SoulPluginChain chain) {
        StaticLog.debug("执行责任链插件", U.format("ServerWebExchange", JSON.toJSON(exchange), "SoulPluginChain", JSON.toJSON(chain)));

        final PluginZkDTO pluginZkDTO = zookeeperCacheManager.findPluginByName(named());
        if (!(skip(exchange) || pluginZkDTO == null || !pluginZkDTO.getEnabled())) {
            //获取selector
            final List<SelectorZkDTO> selectors = zookeeperCacheManager.findSelectorByPluginName(named());
            if (CollectionUtils.isEmpty(selectors)) {
                StaticLog.debug("SelectorZkDTO不存在,执行下一个责任链插件");
                return chain.execute(exchange);
            }
            final SelectorZkDTO selectorZkDTO = selectors.stream()
                    .filter(selector -> selector.getEnabled() && filterSelector(selector, exchange))
                    .findFirst().orElse(null);

            if (Objects.isNull(selectorZkDTO)) {
                StaticLog.debug("不存在有效SelectorZkDTO,执行下一个责任链插件");
                return chain.execute(exchange);
            }

            if (selectorZkDTO.getLoged()) {
                StaticLog.debug(getNamed() + " selector success selector name :{}", selectorZkDTO.getName());

            }
            final List<RuleZkDTO> rules = zookeeperCacheManager.findRuleBySelectorId(selectorZkDTO.getId());
            if (CollectionUtils.isEmpty(rules)) {
                StaticLog.debug("RuleZkDTO不存在,执行下一个责任链插件");
                return chain.execute(exchange);
            }

            RuleZkDTO rule = filterRule(exchange, rules);

            final RequestDTO request = exchange.getAttribute(Constants.REQUESTDTO);

            if (Objects.isNull(rule)) {
                //If the divide or dubbo or spring cloud plug-in does not match, return directly
                if (PluginEnum.DIVIDE.getName().equals(named())
                        || PluginEnum.DUBBO.getName().equals(named())
                        || PluginEnum.SPRING_CLOUD.getName().equals(named())) {
                    LogUtils.info(LOGGER, () -> Objects.requireNonNull(request).getModule() + ":"
                            + request.getMethod() + " not match  " + named() + "  rule");
                    final SoulResult error = SoulResult.error(HttpStatus.NOT_FOUND.value(),
                            Constants.UPSTREAM_NOT_FIND);
                    return exchange.getResponse()
                            .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                                    .wrap(Objects.requireNonNull(JsonUtils.toJson(error)).getBytes())));
                }
                return chain.execute(exchange);
            }

            if (rule.getLoged()) {
                LogUtils.info(LOGGER, () -> Objects.requireNonNull(request).getModule() + ":" + request.getMethod() + " match " + named()
                        + " rule is name :"
                        + rule.getName());
            }
            return doExecute(exchange, chain, selectorZkDTO, rule);
        }
        return chain.execute(exchange);
    }

    private Boolean filterSelector(final SelectorZkDTO selector, final ServerWebExchange exchange) {
        if (selector.getType() == SelectorTypeEnum.CUSTOM_FLOW.getCode()) {
            if (CollectionUtils.isEmpty(selector.getConditionZkDTOList())) {
                return false;
            }
            return MatchStrategyFactory.of(selector.getMatchMode())
                    .match(selector.getConditionZkDTOList(), exchange);
        }
        return true;
    }

    private RuleZkDTO filterRule(final ServerWebExchange exchange, final List<RuleZkDTO> rules) {
        return rules.stream()
                .filter(rule -> Objects.nonNull(rule) && rule.getEnabled())
                .filter((RuleZkDTO ruleZkDTO) -> MatchStrategyFactory.of(ruleZkDTO.getMatchMode())
                        .match(ruleZkDTO.getConditionZkDTOList(), exchange))
                .findFirst().orElse(null);
    }

    /**
     * this is Template Method child has Implement your own logic.
     *
     * @param exchange exchange the current server exchange {@linkplain ServerWebExchange}
     * @param chain    chain the current chain  {@linkplain ServerWebExchange}
     * @param selector selector    {@linkplain SelectorZkDTO}
     * @param rule     rule    {@linkplain RuleZkDTO}
     * @return {@code Mono<Void>} to indicate when request handling is complete
     */
    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, SoulPluginChain chain, SelectorZkDTO selector, RuleZkDTO rule);

    @Override
    public PluginTypeEnum getPluginType() {
        return pluginType();
    }

    @Override
    public String getNamed() {
        return named();
    }
}
