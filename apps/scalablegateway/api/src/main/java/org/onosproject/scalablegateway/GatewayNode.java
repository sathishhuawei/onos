/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.scalablegateway;

import com.google.common.collect.ImmutableList;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents SONA GatewayNode information.
 */
public final class GatewayNode {
    private final DeviceId gatewayDeviceId;
    private final List<String> gatewayExternalInterfaceNames;
    private final Ip4Address dataIpAddress;

    private GatewayNode(DeviceId gatewayDeviceId, List<String> gatewayExternalInterfaceNames,
                        Ip4Address dataIpAddress) {
        this.gatewayDeviceId = gatewayDeviceId;
        this.gatewayExternalInterfaceNames = gatewayExternalInterfaceNames;
        this.dataIpAddress = dataIpAddress;
    }

    /**
     * Returns the device id of gateway node.
     *
     * @return The device id of gateway node
     */
    public DeviceId getGatewayDeviceId() {
        return gatewayDeviceId;
    }

    /**
     * Returns the list of gateway`s interface names.
     *
     * @return The list of interface names
     */
    public List<String> getGatewayExternalInterfaceNames() {
        return ImmutableList.copyOf(gatewayExternalInterfaceNames);
    }

    /**
     * Returns the data ip address of gateway node.
     *
     * @return The data ip address of gateway node
     */
    public Ip4Address getDataIpAddress() {
        return dataIpAddress;
    }

    /**
     * GatewayNode Builder class.
     */
    public static final class Builder {

        private DeviceId gatewayDeviceId;
        private List<String> gatewayExternalInterfaceNames;
        private Ip4Address dataIpAddress;

        /**
         * Sets the device id of gateway node.
         *
         * @param deviceId The device id of gateway node
         * @return Builder object
         */
        public Builder gatewayDeviceId(DeviceId deviceId) {
            this.gatewayDeviceId = deviceId;
            return this;
        }

        /**
         * Sets the list of gateway`s interface names.
         *
         * @param names The list of gateway`s interface name
         * @return Builder object
         */
        public Builder gatewayExternalInterfaceNames(List<String> names) {
            this.gatewayExternalInterfaceNames = names;
            return this;
        }

        /**
         * Sets the ip address of gateway node for data plain.
         *
         * @param address The ip address of gateway node
         * @return Builder object
         */
        public Builder dataIpAddress(Ip4Address address) {
            this.dataIpAddress = address;
            return this;
        }

        /**
         * Builds a GatewayNode object.
         *
         * @return GatewayNode object
         */
        public GatewayNode build() {
            return new GatewayNode(checkNotNull(gatewayDeviceId), checkNotNull(gatewayExternalInterfaceNames),
                    checkNotNull(dataIpAddress));
        }
    }
}
