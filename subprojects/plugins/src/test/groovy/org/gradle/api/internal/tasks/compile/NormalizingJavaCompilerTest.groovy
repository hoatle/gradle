/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.internal.tasks.compile

import spock.lang.Specification
import org.gradle.api.tasks.WorkResult
import org.gradle.api.file.FileCollection
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.compile.CompileOptions

class NormalizingJavaCompilerTest extends Specification {
    final Compiler<JavaCompileSpec> target = Mock()
    final CompileOptions options = Mock()
    final JavaCompileSpec spec = Mock()
    final NormalizingJavaCompiler compiler = new NormalizingJavaCompiler(target)

    def setup() {
        _ * spec.compileOptions >> options
    }

    def "delegates to target compiler after resolving source and classpath"() {
        WorkResult workResult = Mock()
        _ * spec.source >> files(new File("source.java"))
        _ * spec.classpath >> files()

        when:
        def result = compiler.execute(spec)

        then:
        result == workResult

        and:
        1 * spec.setSource(!null)
        1 * spec.setClasspath(!null)

        and:
        1 * target.execute(spec) >> workResult
    }

    def "fails when a non-Java source file provided"() {
        _ * spec.source >> files(new File("source.txt"))
        _ * spec.classpath >> files()

        when:
        compiler.execute(spec)

        then:
        InvalidUserDataException e = thrown()
        e.message == 'Cannot compile non-Java source file \'source.txt\'.'

        and:
        0 * target._
    }

    def "propagates compile failure when failOnError is true"() {
        CompilationFailedException failure = new CompilationFailedException()
        _ * spec.source >> files(new File("source.java"))
        _ * spec.classpath >> files()
        _ * options.failOnError >> true

        when:
        compiler.execute(spec)
        
        then:
        CompilationFailedException e = thrown()
        e == failure
        
        and:
        1 * target.execute(spec) >> { throw failure }
    }

    def "ignores compile failure when failOnError is false"() {
        CompilationFailedException failure = new CompilationFailedException()
        _ * spec.source >> files(new File("source.java"))
        _ * spec.classpath >> files()

        when:
        def result = compiler.execute(spec)

        then:
        !result.didWork

        and:
        1 * target.execute(spec) >> { throw failure }
    }

    def "propagates other failure"() {
        RuntimeException failure = new RuntimeException()
        _ * spec.source >> files(new File("source.java"))
        _ * spec.classpath >> files()

        when:
        compiler.execute(spec)

        then:
        RuntimeException e = thrown()
        e == failure

        and:
        1 * target.execute(spec) >> { throw failure }
    }

    def files(File... files) {
        FileCollection collection = Mock()
        _ * collection.files >> { files as Set }
        _ * collection.iterator() >> { (files as List).iterator() }
        return collection
    }
}
