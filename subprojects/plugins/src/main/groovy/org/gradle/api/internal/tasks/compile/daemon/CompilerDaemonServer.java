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
package org.gradle.api.internal.tasks.compile.daemon;

import org.gradle.api.Action;
import org.gradle.api.internal.tasks.compile.CompileSpec;
import org.gradle.api.internal.tasks.compile.Compiler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.UncheckedException;
import org.gradle.process.internal.WorkerProcessContext;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;


public class CompilerDaemonServer implements Action<WorkerProcessContext>, CompilerDaemonServerProtocol, Serializable {
    private static final Logger LOGGER = Logging.getLogger(CompilerDaemonServer.class);
    
    private volatile CompilerDaemonClientProtocol client;
    private volatile CountDownLatch stop;
    
    public void execute(WorkerProcessContext context) {
        client = context.getServerConnection().addOutgoing(CompilerDaemonClientProtocol.class);
        stop = new CountDownLatch(1);
        context.getServerConnection().addIncoming(CompilerDaemonServerProtocol.class, this);
        try {
            stop.await();
        } catch (InterruptedException e) {
            throw UncheckedException.asUncheckedException(e);
        }

    }

    public <T extends CompileSpec> void execute(Compiler<T> compiler, T spec) {
        try {
            LOGGER.info("Executing {}.", compiler);
            WorkResult result = compiler.execute(spec);
            LOGGER.info("Successfully executed {}.", compiler);
            client.executed(new CompileResult(result.getDidWork(), null));
        } catch (Throwable t) {
            LOGGER.info("Exception executing {}: {}.", compiler, t);
            client.executed(new CompileResult(true, t));
        }
    }

    public void stop() {
        stop.countDown();
    }
}
