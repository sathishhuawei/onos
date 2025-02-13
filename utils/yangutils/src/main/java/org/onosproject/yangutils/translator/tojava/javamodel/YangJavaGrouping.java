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
package org.onosproject.yangutils.translator.tojava.javamodel;

import java.io.IOException;

import org.onosproject.yangutils.datamodel.YangGrouping;
import org.onosproject.yangutils.translator.tojava.JavaCodeGenerator;
import org.onosproject.yangutils.translator.tojava.JavaFileInfo;
import org.onosproject.yangutils.translator.tojava.TempJavaCodeFragmentFiles;
import org.onosproject.yangutils.translator.tojava.utils.YangPluginConfig;

/**
 * Represents grouping information extended to support java code generation.
 */
public class YangJavaGrouping
        extends YangGrouping
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    /**
     * Creates YANG Java grouping object.
     */
    public YangJavaGrouping() {
        super();
    }

    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin)
            throws IOException {
        /*Do nothing, the uses will copy the contents to the used location*/
    }

    @Override
    public void generateCodeExit()
            throws IOException {
        /*Do nothing, the uses will copy the contents to the used location*/
    }

    @Override
    public JavaFileInfo getJavaFileInfo() {
        /*Do nothing, the uses will copy the contents to the used location*/
        return null;
    }

    @Override
    public void setJavaFileInfo(JavaFileInfo javaInfo) {
        /*Do nothing, the uses will copy the contents to the used location*/
    }

    @Override
    public TempJavaCodeFragmentFiles getTempJavaCodeFragmentFiles() {
        /*Do nothing, the uses will copy the contents to the used location*/
        return null;
    }

    @Override
    public void setTempJavaCodeFragmentFiles(TempJavaCodeFragmentFiles fileHandle) {
        /*Do nothing, the uses will copy the contents to the used location*/
    }
}
