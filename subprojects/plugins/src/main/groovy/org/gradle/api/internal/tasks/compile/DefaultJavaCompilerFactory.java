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
package org.gradle.api.internal.tasks.compile;

import org.gradle.api.AntBuilder;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.compile.daemon.DaemonJavaCompiler;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.internal.Factory;

public class DefaultJavaCompilerFactory implements JavaCompilerFactory {
    private final ProjectInternal project;
    private final Factory<AntBuilder> antBuilderFactory;
    private final JavaCompilerFactory inProcessCompilerFactory;

    public DefaultJavaCompilerFactory(ProjectInternal project, Factory<AntBuilder> antBuilderFactory, JavaCompilerFactory inProcessCompilerFactory){
        this.project = project;
        this.antBuilderFactory = antBuilderFactory;
        this.inProcessCompilerFactory = inProcessCompilerFactory;
    }

    public Compiler<JavaCompileSpec> create(CompileOptions options) {
        if (options.isUseAnt()) {
            return new AntJavaCompiler(antBuilderFactory);
        }
        return new NormalizingJavaCompiler(createTargetCompiler(options));
    }

    private Compiler<JavaCompileSpec> createTargetCompiler(CompileOptions options) {
        Compiler<JavaCompileSpec> target = inProcessCompilerFactory.create(options);
        if (options.isFork()) {
            target = new DaemonJavaCompiler(project, target);
        }
        return target;
    }
}
