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

import org.onosproject.yangutils.datamodel.YangModule;
import org.onosproject.yangutils.translator.exception.TranslatorException;
import org.onosproject.yangutils.translator.tojava.JavaCodeGenerator;
import org.onosproject.yangutils.translator.tojava.JavaFileInfo;
import org.onosproject.yangutils.translator.tojava.TempJavaCodeFragmentFiles;
import org.onosproject.yangutils.translator.tojava.utils.YangJavaModelUtils;
import org.onosproject.yangutils.translator.tojava.utils.YangPluginConfig;

import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_INTERFACE_WITH_BUILDER;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_MANAGER_WITH_RPC;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getRootPackage;

/**
 * Represents module information extended to support java code generation.
 */
public class YangJavaModule
        extends YangModule
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    /**
     * Contains the information of the java file being generated.
     */
    private JavaFileInfo javaFileInfo;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates a YANG node of module type.
     */
    public YangJavaModule() {
        super();
        setJavaFileInfo(new JavaFileInfo());
        getJavaFileInfo().setGeneratedFileTypes(GENERATE_MANAGER_WITH_RPC);
    }

    /**
     * Returns the generated java file information.
     *
     * @return generated java file information
     */
    @Override
    public JavaFileInfo getJavaFileInfo() {
        if (javaFileInfo == null) {
            throw new TranslatorException("Missing java info in java datamodel node");
        }
        return javaFileInfo;
    }

    /**
     * Sets the java file info object.
     *
     * @param javaInfo java file info object
     */
    @Override
    public void setJavaFileInfo(JavaFileInfo javaInfo) {
        javaFileInfo = javaInfo;
    }

    /**
     * Returns the temporary file handle.
     *
     * @return temporary file handle
     */
    @Override
    public TempJavaCodeFragmentFiles getTempJavaCodeFragmentFiles() {
        return tempFileHandle;
    }

    /**
     * Sets temporary file handle.
     *
     * @param fileHandle temporary file handle
     */
    @Override
    public void setTempJavaCodeFragmentFiles(TempJavaCodeFragmentFiles fileHandle) {
        tempFileHandle = fileHandle;
    }

    /**
     * Generates java code for module.
     *
     * @param yangPlugin YANG plugin config
     * @throws IOException when fails to generate the source files
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin)
            throws IOException {
        String modulePkg = getRootPackage(getVersion(), getNameSpace().getUri(), getRevision().getRevDate());
        YangJavaModelUtils.generateCodeOfRootNode(this, yangPlugin, modulePkg);
    }

    /**
     * Creates a java file using the YANG module info.
     */
    @Override
    public void generateCodeExit()
            throws IOException {
        getTempJavaCodeFragmentFiles().generateJavaFile(GENERATE_INTERFACE_WITH_BUILDER, this);
    }
}
