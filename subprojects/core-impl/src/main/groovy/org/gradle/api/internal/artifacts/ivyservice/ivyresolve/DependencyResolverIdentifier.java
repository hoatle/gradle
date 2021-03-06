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
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.gradle.api.internal.artifacts.repositories.ResourceCollectionResolver;
import org.gradle.util.GUtil;
import org.gradle.util.hash.HashUtil;

import java.util.ArrayList;
import java.util.List;

public class DependencyResolverIdentifier {
    private final String resolverId;

    public DependencyResolverIdentifier(DependencyResolver resolver) {
        List<String> parts = new ArrayList<String>();
        parts.add(resolver.getClass().getName());
        if (resolver instanceof ResourceCollectionResolver) {
            ResourceCollectionResolver resourceCollectionResolver = (ResourceCollectionResolver) resolver;
            parts.add(joinPatterns(resourceCollectionResolver.getIvyPatterns()));
            parts.add(joinPatterns(resourceCollectionResolver.getArtifactPatterns()));
            if (resourceCollectionResolver.isM2compatible()) {
                parts.add("m2compatible");
            }
        } else if (resolver instanceof AbstractPatternsBasedResolver) {
            AbstractPatternsBasedResolver patternsBasedResolver = (AbstractPatternsBasedResolver) resolver;
            parts.add(joinPatterns(patternsBasedResolver.getIvyPatterns()));
            parts.add(joinPatterns(patternsBasedResolver.getArtifactPatterns()));
            if (patternsBasedResolver.isM2compatible()) {
                parts.add("m2compatible");
            }
        } else {
            parts.add(resolver.getName());
            // TODO We should not be assuming equality between resolvers here based on name...
        }

        resolverId = calculateId(parts);
    }

    private String joinPatterns(List<String> patterns) {
        return GUtil.join(patterns, ",");
    }

    private String calculateId(List<String> parts) {
        String idString = GUtil.join(parts, "::");
        return HashUtil.createHash(idString, "MD5").asHexString();
    }

    public String getId() {
        return resolverId;
    }
}
