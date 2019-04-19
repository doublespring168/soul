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

package org.dromara.soul.web.filter;

import cn.hutool.log.StaticLog;
import org.dromara.soul.common.result.SoulResult;
import org.dromara.soul.common.utils.GsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.doublespring.utils.U;

/**
 * soul webFilter parent.
 *
 * @author xiaoyu
 */
public abstract class AbstractWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        StaticLog.debug("开始执行过滤器链");
        final ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        Mono<Boolean> isPassed = Mono.just(false);
        try {
            isPassed = doFilter(exchange, chain);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            String message = e.getMessage();
            final SoulResult soulResult = SoulResult.error(message);
            StaticLog.debug("未通过filter", message);
            Mono<Void> result = response.writeWith(
                    Mono.just(
                            response.bufferFactory().wrap(GsonUtils.getInstance().toJson(soulResult).getBytes())
                    )
            );
            return result;
        }
        StaticLog.debug("通过filter", U.format("filter name", getFilterName()));
        return chain.filter(exchange);
    }

    /**
     * this is Template Method ,children Implement your own filtering logic.
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     * @return {@code Mono<Boolean>} result：TRUE (is pass)，and flow next filter；FALSE (is not pass) execute doDenyResponse(ServerWebExchange exchange)
     */
    protected abstract Mono<Boolean> doFilter(ServerWebExchange exchange, WebFilterChain chain);

    /**
     * this is Template Method ,children Implement your own And response client.
     *
     * @param exchange the current server exchange.
     * @return {@code Mono<Void>} response msg.
     */
    protected abstract Mono<Void> doDenyResponse(ServerWebExchange exchange);

    /**
     * 功能说明：获取子类类名称
     * Author：spring
     * Date：2019-04-19 18:21
     */
    public String getFilterName() {
        return this.getClass().getSimpleName();
    }
}
