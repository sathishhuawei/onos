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
package org.onosproject.net.optical.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import org.onosproject.net.CltSignalType;
import org.onosproject.net.Port;
import org.onosproject.net.optical.OduCltPort;
import org.onosproject.net.optical.utils.ForwardingPort;

import com.google.common.annotations.Beta;

/**
 * Implementation of ODU client port (Optical channel Data Unit).
 * Also referred to as a T-port or wide band port.
 * See ITU G.709 "Interfaces for the Optical Transport Network (OTN)"
 */
@Beta
public class DefaultOduCltPort extends ForwardingPort implements OduCltPort {

    private final CltSignalType signalType;

    /**
     * Creates an ODU client port.
     *
     * @param delegate      Port
     * @param signalType        ODU client signal type
     */
    public DefaultOduCltPort(Port delegate, CltSignalType signalType) {
        super(delegate);
        this.signalType = checkNotNull(signalType);
    }

    @Override
    public Type type() {
        return Type.ODUCLT;
    }

    @Override
    public long portSpeed() {
        return signalType().bitRate();
    }

    @Override
    public CltSignalType signalType() {
        return signalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                            signalType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj != null && getClass() == obj.getClass()) {
            final DefaultOduCltPort that = (DefaultOduCltPort) obj;
            return super.toEqualsBuilder(that)
                    .append(this.signalType(), that.signalType())
                    .isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toStringHelper()
                .add("signalType", signalType())
                .toString();
    }

}
