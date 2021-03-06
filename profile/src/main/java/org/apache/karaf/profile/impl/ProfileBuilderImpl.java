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
package org.apache.karaf.profile.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.karaf.profile.Profile;
import org.apache.karaf.profile.ProfileBuilder;

import static org.apache.karaf.profile.impl.ProfileImpl.ConfigListType;

/**
 * The default {@link ProfileBuilder}
 */
public final class ProfileBuilderImpl implements ProfileBuilder {

    private static final String PARENTS_ATTRIBUTE_KEY = Profile.ATTRIBUTE_PREFIX + Profile.PARENTS;

	private String profileId;
	private Map<String, byte[]> fileMapping = new HashMap<>();
	private boolean isOverlay;
	
	@Override
	public ProfileBuilder from(Profile profile) {
		profileId = profile.getId();
		setFileConfigurations(profile.getFileConfigurations());
        return this;
	}

	@Override
	public ProfileBuilder identity(String profileId) {
		this.profileId = profileId;
		return this;
	}

	@Override
    public List<String> getParents() {
        Map<String, String> config = getConfigurationInternal(Profile.INTERNAL_PID);
        String pspec = config.get(PARENTS_ATTRIBUTE_KEY);
        String[] parentIds = pspec != null ? pspec.split(" ") : new String[0];
        return Arrays.asList(parentIds);
    }

    @Override
	public ProfileBuilder addParent(String parentId) {
        return addParentsInternal(Collections.singletonList(parentId), false);
	}

	@Override
	public ProfileBuilder addParents(List<String> parentIds) {
		return addParentsInternal(parentIds, false);
	}

    @Override
    public ProfileBuilder setParents(List<String> parentIds) {
        return addParentsInternal(parentIds, true);
    }

    private ProfileBuilder addParentsInternal(List<String> parentIds, boolean clear) {
        Set<String> currentIds = new LinkedHashSet<>(getParents());
        if (clear) {
            currentIds.clear();
        }
        if (parentIds != null) {
            currentIds.addAll(parentIds);
        }
        updateParentsAttribute(currentIds);
        return this;
    }
    
    @Override
	public ProfileBuilder removeParent(String profileId) {
        Set<String> currentIds = new LinkedHashSet<>(getParents());
        currentIds.remove(profileId);
        updateParentsAttribute(currentIds);
		return this;
	}

    private void updateParentsAttribute(Collection<String> parentIds) {
        Map<String, String> config = getConfigurationInternal(Profile.INTERNAL_PID);
        config.remove(PARENTS_ATTRIBUTE_KEY);
        if (parentIds.size() > 0) {
            config.put(PARENTS_ATTRIBUTE_KEY, parentsAttributeValue(parentIds));
        }
        addConfiguration(Profile.INTERNAL_PID, config);
    }

    private String parentsAttributeValue(Collection<String> parentIds) {
        String pspec = "";
        if (parentIds.size() > 0) {
            for (String parentId : parentIds) {
                pspec += " " + parentId;
            }
            pspec = pspec.substring(1);
        }
        return pspec;
    }
    
    @Override
    public Set<String> getFileConfigurationKeys() {
        return fileMapping.keySet();
    }

    @Override
    public byte[] getFileConfiguration(String key) {
        return fileMapping.get(key);
    }

	@Override
	public ProfileBuilder setFileConfigurations(Map<String, byte[]> configurations) {
		fileMapping = new HashMap<>(configurations);
		return this;
	}

    @Override
    public ProfileBuilder addFileConfiguration(String fileName, byte[] data) {
        fileMapping.put(fileName, data);
        return this;
    }

    @Override
    public ProfileBuilder deleteFileConfiguration(String fileName) {
        fileMapping.remove(fileName);
        return this;
    }

	@Override
	public ProfileBuilder setConfigurations(Map<String, Map<String, String>> configs) {
	    for (String pid : getConfigurationKeys()) {
	        deleteConfiguration(pid);
	    }
		for (Entry<String, Map<String, String>> entry : configs.entrySet()) {
		    addConfiguration(entry.getKey(), new HashMap<>(entry.getValue()));
		}
		return this;
	}

    @Override
    public ProfileBuilder addConfiguration(String pid, Map<String, String> config) {
        fileMapping.put(pid + Profile.PROPERTIES_SUFFIX, Utils.toBytes(config));
        return this;
    }

    @Override
    public ProfileBuilder addConfiguration(String pid, String key, String value) {
        Map<String, String> config = getConfigurationInternal(pid);
        config.put(key, value);
        return addConfiguration(pid, config);
    }

    @Override
    public Set<String> getConfigurationKeys() {
        Set<String> result = new HashSet<>();
        for (String fileKey : fileMapping.keySet()) {
            if (fileKey.endsWith(Profile.PROPERTIES_SUFFIX)) {
                String configKey = fileKey.substring(0, fileKey.indexOf(Profile.PROPERTIES_SUFFIX));
                result.add(configKey);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Map<String, String> getConfiguration(String pid) {
        Map<String, String> config = getConfigurationInternal(pid);
        return Collections.unmodifiableMap(config);
    }

    private Map<String, String> getConfigurationInternal(String pid) {
        byte[] bytes = fileMapping.get(pid + Profile.PROPERTIES_SUFFIX);
        return Utils.toProperties(bytes);
    }
    
    @Override
    public ProfileBuilder deleteConfiguration(String pid) {
        fileMapping.remove(pid + Profile.PROPERTIES_SUFFIX);
        return this;
    }
    
	@Override
	public ProfileBuilder setBundles(List<String> values) {
		addAgentConfiguration(ConfigListType.BUNDLES, values);
		return this;
	}

    @Override
    public ProfileBuilder addBundle(String value) {
        addAgentConfiguration(ConfigListType.BUNDLES, value);
        return this;
    }

    @Override
	public ProfileBuilder setFeatures(List<String> values) {
		addAgentConfiguration(ConfigListType.FEATURES, values);
		return this;
	}

    @Override
    public ProfileBuilder addFeature(String value) {
        addAgentConfiguration(ConfigListType.FEATURES, value);
        return this;
    }

    @Override
	public ProfileBuilder setRepositories(List<String> values) {
		addAgentConfiguration(ConfigListType.REPOSITORIES, values);
		return this;
	}

    @Override
    public ProfileBuilder addRepository(String value) {
        addAgentConfiguration(ConfigListType.REPOSITORIES, value);
        return this;
    }

    @Override
	public ProfileBuilder setOverrides(List<String> values) {
		addAgentConfiguration(ConfigListType.OVERRIDES, values);
		return this;
	}

    @Override
    public ProfileBuilder setOptionals(List<String> values) {
        addAgentConfiguration(ConfigListType.OPTIONALS, values);
        return this;
    }

	public ProfileBuilder setOverlay(boolean overlay) {
		this.isOverlay = overlay;
		return this;
	}

	@Override
    public ProfileBuilder addAttribute(String key, String value) {
        addConfiguration(Profile.INTERNAL_PID, Profile.ATTRIBUTE_PREFIX + key, value);
        return this;
    }

    @Override
    public ProfileBuilder setAttributes(Map<String, String> attributes) {
        Map<String, String> config = getConfigurationInternal(Profile.INTERNAL_PID);
        for (String key : new ArrayList<>(config.keySet())) {
            if (key.startsWith(Profile.ATTRIBUTE_PREFIX)) {
                config.remove(key);
            }
        }
        for (Entry<String, String> entry : attributes.entrySet()) {
            config.put(Profile.ATTRIBUTE_PREFIX + entry.getKey(), entry.getValue());
        }
        addConfiguration(Profile.INTERNAL_PID, config);
        return null;
    }

    private void addAgentConfiguration(ConfigListType type, List<String> values) {
        String prefix = type + ".";
        Map<String, String> config = getConfigurationInternal(Profile.INTERNAL_PID);
        for (String key : new ArrayList<>(config.keySet())) {
            if (key.startsWith(prefix)) {
                config.remove(key);
            }
        }
        for (String value : values) {
            config.put(prefix + value, value);
        }
        addConfiguration(Profile.INTERNAL_PID, config);
    }

    private void addAgentConfiguration(ConfigListType type, String value) {
        String prefix = type + ".";
        Map<String, String> config = getConfigurationInternal(Profile.INTERNAL_PID);
        config.put(prefix + value, value);
        addConfiguration(Profile.INTERNAL_PID, config);
    }


    @Override
	public Profile getProfile() {
		return new ProfileImpl(profileId, getParents(), fileMapping, isOverlay);
	}

}
