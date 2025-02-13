/*
 * Copyright 2014-present Open Networking Laboratory
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
package org.onosproject.store.device.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.net.DefaultAnnotations.union;
import static org.onosproject.net.optical.device.OchPortHelper.ochPortDescription;
import static org.onosproject.net.optical.device.OduCltPortHelper.oduCltPortDescription;
import static org.onosproject.net.optical.device.OmsPortHelper.omsPortDescription;
import static org.onosproject.net.optical.device.OtuPortHelper.otuPortDescription;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.OchPortDescription;
import org.onosproject.net.device.OduCltPortDescription;
import org.onosproject.net.device.OmsPortDescription;
import org.onosproject.net.device.OtuPortDescription;
import org.onosproject.net.device.PortDescription;
import org.onosproject.store.Timestamp;
import org.onosproject.store.impl.Timestamped;

/*
 * Collection of Description of a Device and Ports, given from a Provider.
 */
class DeviceDescriptions {

    private volatile Timestamped<DeviceDescription> deviceDesc;

    private final ConcurrentMap<PortNumber, Timestamped<PortDescription>> portDescs;

    public DeviceDescriptions(Timestamped<DeviceDescription> desc) {
        this.deviceDesc = checkNotNull(desc);
        this.portDescs = new ConcurrentHashMap<>();
    }

    public Timestamp getLatestTimestamp() {
        Timestamp latest = deviceDesc.timestamp();
        for (Timestamped<PortDescription> desc : portDescs.values()) {
            if (desc.timestamp().compareTo(latest) > 0) {
                latest = desc.timestamp();
            }
        }
        return latest;
    }

    public Timestamped<DeviceDescription> getDeviceDesc() {
        return deviceDesc;
    }

    public Timestamped<PortDescription> getPortDesc(PortNumber number) {
        return portDescs.get(number);
    }

    public Map<PortNumber, Timestamped<PortDescription>> getPortDescs() {
        return Collections.unmodifiableMap(portDescs);
    }

    /**
     * Puts DeviceDescription, merging annotations as necessary.
     *
     * @param newDesc new DeviceDescription
     */
    public void putDeviceDesc(Timestamped<DeviceDescription> newDesc) {
        Timestamped<DeviceDescription> oldOne = deviceDesc;
        Timestamped<DeviceDescription> newOne = newDesc;
        if (oldOne != null) {
            SparseAnnotations merged = union(oldOne.value().annotations(),
                                             newDesc.value().annotations());
            newOne = new Timestamped<>(
                    new DefaultDeviceDescription(newDesc.value(), merged),
                    newDesc.timestamp());
        }
        deviceDesc = newOne;
    }

    /**
     * Puts PortDescription, merging annotations as necessary.
     *
     * @param newDesc new PortDescription
     */
    public void putPortDesc(Timestamped<PortDescription> newDesc) {
        Timestamped<PortDescription> oldOne = portDescs.get(newDesc.value().portNumber());
        Timestamped<PortDescription> newOne = newDesc;
        if (oldOne != null) {
            SparseAnnotations merged = union(oldOne.value().annotations(),
                                             newDesc.value().annotations());
            newOne = null;
            switch (newDesc.value().type()) {
                case OMS:
                    if (newDesc.value() instanceof OmsPortDescription) {
                        // remove if-block after deprecation is complete
                        OmsPortDescription omsDesc = (OmsPortDescription) (newDesc.value());
                        newOne = new Timestamped<>(
                                omsPortDescription(omsDesc,
                                                   omsDesc.minFrequency(),
                                                   omsDesc.maxFrequency(),
                                                   omsDesc.grid(), merged),
                                newDesc.timestamp());
                    } else {
                        // same as default case
                        newOne = new Timestamped<>(
                                new DefaultPortDescription(newDesc.value(), merged),
                                newDesc.timestamp());
                    }
                    break;
                case OCH:
                    if (newDesc.value() instanceof OchPortDescription) {
                        // remove if-block after Och related deprecation is complete
                        OchPortDescription ochDesc = (OchPortDescription) (newDesc.value());
                        newOne = new Timestamped<>(
                                ochPortDescription(ochDesc,
                                                   ochDesc.signalType(),
                                                   ochDesc.isTunable(),
                                                   ochDesc.lambda(), merged),
                                newDesc.timestamp());
                    } else {
                        // same as default case
                        newOne = new Timestamped<>(
                                new DefaultPortDescription(newDesc.value(), merged),
                                newDesc.timestamp());
                    }
                    break;
                case ODUCLT:
                    if (newDesc.value() instanceof OduCltPortDescription) {
                        // remove if-block after deprecation is complete
                        OduCltPortDescription ocDesc = (OduCltPortDescription) (newDesc.value());
                        newOne = new Timestamped<>(
                                oduCltPortDescription(ocDesc,
                                                      ocDesc.signalType(),
                                                      merged),
                                newDesc.timestamp());
                    } else {
                        // same as default case
                        newOne = new Timestamped<>(
                                new DefaultPortDescription(newDesc.value(), merged),
                                newDesc.timestamp());
                    }
                    break;
                case OTU:
                    if (newDesc.value() instanceof OtuPortDescription) {
                        // remove if-block after deprecation is complete
                        OtuPortDescription otuDesc = (OtuPortDescription) (newDesc.value());
                        newOne = new Timestamped<>(
                                otuPortDescription(
                                                   otuDesc, otuDesc.signalType(), merged),
                                newDesc.timestamp());
                    } else {
                        // same as default case
                        newOne = new Timestamped<>(
                                new DefaultPortDescription(newDesc.value(), merged),
                                newDesc.timestamp());
                    }
                    break;
                default:
                    newOne = new Timestamped<>(
                            new DefaultPortDescription(newDesc.value(), merged),
                            newDesc.timestamp());
            }
        }
        portDescs.put(newOne.value().portNumber(), newOne);
    }
}
