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

package org.onosproject.bmv2.api.runtime;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of a BMv2 device.
 */
public final class Bmv2Device {

    private final String thriftServerHost;
    private final int thriftServerPort;
    private final int internalDeviceId;

    /**
     * Creates a new Bmv2 device object.
     *
     * @param thriftServerHost the host of the Thrift runtime server running inside the device
     * @param thriftServerPort the port of the Thrift runtime server running inside the device
     * @param internalDeviceId the internal device id
     */
    public Bmv2Device(String thriftServerHost, int thriftServerPort, int internalDeviceId) {
        this.thriftServerHost = checkNotNull(thriftServerHost, "host cannot be null");
        this.thriftServerPort = checkNotNull(thriftServerPort, "port cannot be null");
        this.internalDeviceId = internalDeviceId;
    }

    /**
     * Returns the hostname (or IP address) of the Thrift runtime server running inside the device.
     *
     * @return a string value
     */
    public String thriftServerHost() {
        return thriftServerHost;
    }

    /**
     * Returns the port of the Thrift runtime server running inside the device.
     *
     * @return an integer value
     */
    public int thriftServerPort() {
        return thriftServerPort;
    }

    /**
     * Returns the BMv2-internal device ID, which is an integer arbitrary chosen at device boot.
     * Such an ID must not be confused with the ONOS-internal {@link org.onosproject.net.DeviceId}.
     *
     * @return an integer value
     */
    public int getInternalDeviceId() {
        return internalDeviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thriftServerHost, thriftServerPort, internalDeviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Bmv2Device other = (Bmv2Device) obj;
        return Objects.equal(this.thriftServerHost, other.thriftServerHost)
                && Objects.equal(this.thriftServerPort, other.thriftServerPort)
                && Objects.equal(this.internalDeviceId, other.internalDeviceId);
    }

    @Override
    public String toString() {
        return thriftServerHost + ":" + thriftServerPort + "/" + internalDeviceId;
    }
}
