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

package org.onosproject.drivers.lumentum;

import org.apache.felix.scr.annotations.Component;
import org.onosproject.net.driver.AbstractDriverLoader;
import org.onosproject.net.optical.OpticalDevice;
import org.onosproject.net.optical.device.DefaultOpticalDevice;

/**
 * Loader for Lumentum device drivers from specific xml.
 */
@Component(immediate = true)
public class LumentumDriversLoader extends AbstractDriverLoader {

    // OSGI: help bundle plugin discover runtime package dependency.
    @SuppressWarnings("unused")
    private OpticalDevice optical;
    @SuppressWarnings("unused")
    private DefaultOpticalDevice driver;

    public LumentumDriversLoader() {
        super("/lumentum-drivers.xml");
    }
}
