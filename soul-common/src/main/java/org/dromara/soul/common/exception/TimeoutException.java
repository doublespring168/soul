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

package org.dromara.soul.common.exception;

/**
 * 功能说明：请求时间超时校验
 * Author：spring
 * Date：2019-04-19 17:07
 */
public class TimeoutException extends RuntimeException {

    private static final long serialVersionUID = 8068509879445395353L;

    /**
     * Instantiates a new Soul exception.
     *
     * @param e the e
     */
    public TimeoutException(final Throwable e) {
        super(e);
    }

    /**
     * Instantiates a new Soul exception.
     *
     * @param message the message
     */
    public TimeoutException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new Soul exception.
     *
     * @param message   the message
     * @param throwable the throwable
     */
    public TimeoutException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
