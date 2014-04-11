/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.features.internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.karaf.features.internal.resolver.CapabilitySet;
import org.apache.karaf.features.internal.resolver.SimpleFilter;
import org.osgi.framework.Constants;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

public class RequirementSort<T extends Resource>  {

    /**
     * Sorts {@link Resource} based on their {@link Requirement}s and {@link Capability}s.
     */
    public static <T extends Resource> Collection<T> sort(Collection<T> resources) {
        Set<String> namespaces = new HashSet<String>();
        for (Resource r : resources) {
            for (Capability cap : r.getCapabilities(null)) {
                namespaces.add(cap.getNamespace());
            }
        }
        CapabilitySet capSet = new CapabilitySet(new ArrayList<String>(namespaces));
        for (Resource r : resources) {
            for (Capability cap : r.getCapabilities(null)) {
                capSet.addCapability(cap);
            }
        }
        Set<T> sorted = new LinkedHashSet<T>();
        Set<T> visited = new LinkedHashSet<T>();
        for (T r : resources) {
            visit(r, visited, sorted, capSet);
        }
        return sorted;
    }


    private static <T extends Resource> void visit(T resource, Set<T> visited, Set<T> sorted, CapabilitySet capSet) {
        if (!visited.add(resource)) {
            return;
        }
        for (T r : collectDependencies(resource, capSet)) {
            visit(r, visited, sorted, capSet);
        }
        sorted.add(resource);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Resource> Set<T> collectDependencies(T resource, CapabilitySet capSet) {
        Set<T> result = new LinkedHashSet<T>();
        for (Requirement requirement : resource.getRequirements(null)) {
            String filter = requirement.getDirectives().get(Constants.FILTER_DIRECTIVE);
            SimpleFilter sf = (filter != null)
                    ? SimpleFilter.parse(filter)
                    : new SimpleFilter(null, null, SimpleFilter.MATCH_ALL);
            for (Capability cap : capSet.match(sf, true)) {
                result.add((T) cap.getResource());
            }
        }
        return result;
    }

}
