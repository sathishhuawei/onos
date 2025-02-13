/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.incubator.net.virtual;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.provider.ProviderService;

/**
 * Service through which virtual network providers can inject information into
 * the core.
 */
public interface VirtualNetworkProviderService extends ProviderService<VirtualNetworkProvider> {

    /**
     * This method is used to notify the VirtualNetwork service that a tunnel is now ACTIVE.
     *
     * @param networkId network identifier
     * @param src       source connection point
     * @param dst       destination connection point
     */
    void tunnelUp(NetworkId networkId, ConnectPoint src, ConnectPoint dst);

    /**
     * This method is used to notify the VirtualNetwork service that a tunnel is now
     * FAILED or INACTIVE.
     *
     * @param networkId network identifier
     * @param src       source connection point
     * @param dst       destination connection point
     */
    void tunnelDown(NetworkId networkId, ConnectPoint src, ConnectPoint dst);

}
