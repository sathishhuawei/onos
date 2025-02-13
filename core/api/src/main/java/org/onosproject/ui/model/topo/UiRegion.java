/*
 *  Copyright 2016-present Open Networking Laboratory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onosproject.ui.model.topo;

import org.onosproject.net.region.Region;
import org.onosproject.net.region.RegionId;

import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a region.
 */
public class UiRegion extends UiNode {

    private final Set<UiDevice> uiDevices = new TreeSet<>();
    private final Set<UiHost> uiHosts = new TreeSet<>();
    private final Set<UiLink> uiLinks = new TreeSet<>();

    private Region region;


    @Override
    protected void destroy() {
        uiDevices.forEach(UiDevice::destroy);
        uiHosts.forEach(UiHost::destroy);
        uiLinks.forEach(UiLink::destroy);

        uiDevices.clear();
        uiHosts.clear();
        uiLinks.clear();

        region = null;
    }

    /**
     * Returns the identity of the region.
     *
     * @return region ID
     */
    public RegionId id() {
        return region.id();
    }

    @Override
    public String idAsString() {
        return id().toString();
    }
}
