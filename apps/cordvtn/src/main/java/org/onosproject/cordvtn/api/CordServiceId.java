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
package org.onosproject.cordvtn.api;

import org.onlab.util.Identifier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Representation of service identifier.
 */
public final class CordServiceId extends Identifier<String> {
    /**
     * Default constructor.
     *
     * @param id service identifier
     */
    private CordServiceId(String id) {
        super(id);
    }

    /**
     * Returns the CordServiceId with value.
     *
     * @param id service id
     * @return CordServiceId
     */
    public static CordServiceId of(String id) {
        checkNotNull(id);
        return new CordServiceId(id);
    }
}
