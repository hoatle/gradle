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
package org.gradle.plugins.cpp

import org.gradle.integtests.fixtures.*

import org.junit.*
import spock.lang.IgnoreIf
import org.gradle.internal.os.OperatingSystem
import static org.gradle.util.TextUtil.*

@IgnoreIf({!OperatingSystem.current().findInPath("g++") && !OperatingSystem.current().findInPath("cl.exe")})
class CppSamplesIntegrationTest extends AbstractBinariesIntegrationSpec {

    @Rule public final Sample exewithlib = new Sample('cpp/exewithlib')
    @Rule public final Sample dependencies = new Sample('cpp/dependencies')
    @Rule public final Sample exe = new Sample('cpp/exe')

    def "exe with lib"() {
        given:
        sample exewithlib

        when:
        run "installMain"

        then:
        ":exe:compileMain" in executedTasks

        and:
        sharedLibrary("cpp/exewithlib/lib/build/binaries/lib").isFile()
        executable("cpp/exewithlib/exe/build/binaries/exe").isFile()
        executable("cpp/exewithlib/exe/build/install/main/exe").exec().out == toPlatformLineSeparators("Hello, World!\n")
    }
    
    def "dependencies"() {
        given:
        sample dependencies
        
        when:
        run ":lib:uploadArchives", ":exe:uploadArchives"
        
        then:
        ":exe:mainExtractHeaders" in nonSkippedTasks
        ":exe:compileMain" in nonSkippedTasks
        
        and:
        sharedLibrary("cpp/dependencies/lib/build/binaries/lib").isFile()
        executable("cpp/dependencies/exe/build/binaries/exe").isFile()
        file("cpp/dependencies/exe/build/repo/dependencies/exe/1.0/exe-1.0.exe").exists()
    }
    
    def "exe"() {
        given:
        sample exe
        
        when:
        run "installMain"
        
        then:
        ":compileMain" in nonSkippedTasks
        
        and:
        executable("cpp/exe/build/binaries/exe").exec().out == toPlatformLineSeparators("Hello, World!\n")
        executable("cpp/exe/build/install/main/exe").exec().out == toPlatformLineSeparators("Hello, World!\n")
    }
    
}