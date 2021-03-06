/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.fixtures

import org.gradle.util.AvailablePortFinder
import org.jboss.netty.handler.codec.http.HttpRequest
import org.littleshoot.proxy.DefaultHttpProxyServer
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.HttpRequestFilter
import org.littleshoot.proxy.ProxyAuthorizationHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A Proxy Server used for testing that http proxies are correctly supported.
 * This proxy server will forward all requests to the supplied HttpServer. The true target of the request is ignored.
 * This is necessary because we can't force java to use a proxy for localhost addresses (using the default java ProxySelector).
 */
class TestProxyServer {
    private Logger logger = LoggerFactory.getLogger(HttpServer.class)
    private HttpProxyServer proxyServer;
    private HttpServer httpServer;
    int port
    int requestCount

    TestProxyServer(HttpServer httpServer) {
        this.httpServer = httpServer
    }

    void start() {
        port = new AvailablePortFinder().nextAvailable
        String remote = "localhost:${httpServer.port}"
        proxyServer = new DefaultHttpProxyServer(port, [:], remote, null, new HttpRequestFilter() {
            void filter(HttpRequest httpRequest) {
                requestCount++
            }
        })
        proxyServer.start()
    }

    def requireAuthentication(final String expectedUsername, final String expectedPassword) {
        proxyServer.addProxyAuthenticationHandler(new ProxyAuthorizationHandler() {
            boolean authenticate(String username, String password) {
                return username == expectedUsername && password == expectedPassword
            }
        })
    }
}

