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

package org.gradle.integtests.fixtures;

import com.google.common.collect.Lists;
import org.gradle.api.Transformer;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GradleVersion;

import java.util.Arrays;
import java.util.List;

/**
 * by Szczepan Faber, created at: 1/12/12
 */
public class ReleasedVersions {

    //TODO SF - it should feed from releases.xml
    private final static List<String> RELEASED = Arrays.asList(
                "0.8",
                "0.9-rc-3",
                "0.9",
                "0.9.1",
                "0.9.2",
                "1.0-milestone-1",
                "1.0-milestone-2",
                "1.0-milestone-3",
                "1.0-milestone-4",
                "1.0-milestone-5",
                "1.0-milestone-6",
                "1.0-milestone-7",
                "1.0-milestone-8",
                "1.0-milestone-8a");

    private final GradleDistribution current;

    public ReleasedVersions(GradleDistribution current) {
        this.current = current;
    }

    public BasicGradleDistribution getLast() {
        return current.previousVersion(RELEASED.get(RELEASED.size() - 1));
    }

    public List<BasicGradleDistribution> getAll() {
        return CollectionUtils.collect(RELEASED, new Transformer<BasicGradleDistribution, String>() {
            public BasicGradleDistribution transform(String original) {
                return current.previousVersion(original);
            }
        });
    }

    public BasicGradleDistribution getPreviousOf(BasicGradleDistribution distro) {
        GradleVersion distroVersion = GradleVersion.version(distro.getVersion());

        for (String candidate : Lists.reverse(RELEASED)) {
            GradleVersion candidateVersion = GradleVersion.version(candidate);
            if (distroVersion.compareTo(candidateVersion) > 0) {
                return current.previousVersion(candidate);
            }
        }

        throw new RuntimeException("I don't know the previous version of: " + distro);
    }
}
