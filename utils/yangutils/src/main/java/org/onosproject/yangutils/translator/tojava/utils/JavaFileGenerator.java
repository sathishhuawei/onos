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

package org.onosproject.yangutils.translator.tojava.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onosproject.yangutils.datamodel.YangNode;
import org.onosproject.yangutils.translator.tojava.JavaFileInfo;
import org.onosproject.yangutils.translator.tojava.JavaFileInfoContainer;
import org.onosproject.yangutils.translator.tojava.TempJavaCodeFragmentFilesContainer;
import org.onosproject.yangutils.translator.tojava.javamodel.JavaCodeGeneratorInfo;

import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.BUILDER_CLASS_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.BUILDER_INTERFACE_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_ENUM_CLASS;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_RPC_INTERFACE;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_TYPEDEF_CLASS;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.GENERATE_UNION_CLASS;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.IMPL_CLASS_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedJavaFileType.INTERFACE_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.ATTRIBUTES_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.CONSTRUCTOR_FOR_TYPE_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.CONSTRUCTOR_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.ENUM_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.EQUALS_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.FROM_STRING_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.GETTER_FOR_CLASS_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.GETTER_FOR_INTERFACE_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.HASH_CODE_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.OF_STRING_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.RPC_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.SETTER_FOR_CLASS_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.SETTER_FOR_INTERFACE_MASK;
import static org.onosproject.yangutils.translator.tojava.GeneratedTempFileType.TO_STRING_IMPL_MASK;
import static org.onosproject.yangutils.translator.tojava.utils.JavaCodeSnippetGen.getAugmentedInfoAttribute;
import static org.onosproject.yangutils.translator.tojava.utils.JavaFileGeneratorUtils.getDataFromTempFileHandle;
import static org.onosproject.yangutils.translator.tojava.utils.JavaFileGeneratorUtils.getEnumsValueAttribute;
import static org.onosproject.yangutils.translator.tojava.utils.JavaFileGeneratorUtils.initiateJavaFileGeneration;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getCaptialCase;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getSmallCase;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getAddAugmentInfoMethodImpl;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getAugmentInfoListImpl;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getConstructorStart;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getEnumsConstrcutor;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getEqualsMethodClose;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getEqualsMethodOpen;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getFromStringMethodClose;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getFromStringMethodSignature;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getGetter;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getHashCodeMethodClose;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getHashCodeMethodOpen;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getOmitNullValueString;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getRemoveAugmentationImpl;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getToStringMethodClose;
import static org.onosproject.yangutils.translator.tojava.utils.MethodsGenerator.getToStringMethodOpen;
import static org.onosproject.yangutils.translator.tojava.utils.TempJavaCodeFragmentFilesUtils
        .isHasAugmentationExtended;
import static org.onosproject.yangutils.utils.UtilConstants.BUILDER;
import static org.onosproject.yangutils.utils.UtilConstants.CLOSE_CURLY_BRACKET;
import static org.onosproject.yangutils.utils.UtilConstants.COMMA;
import static org.onosproject.yangutils.utils.UtilConstants.EMPTY_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.FOUR_SPACE_INDENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.IMPL;
import static org.onosproject.yangutils.utils.UtilConstants.INT;
import static org.onosproject.yangutils.utils.UtilConstants.NEW_LINE;
import static org.onosproject.yangutils.utils.UtilConstants.PRIVATE;
import static org.onosproject.yangutils.utils.UtilConstants.PUBLIC;
import static org.onosproject.yangutils.utils.UtilConstants.SEMI_COLAN;
import static org.onosproject.yangutils.utils.UtilConstants.SERVICE_METHOD_STRING;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.GETTER_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.TYPE_CONSTRUCTOR;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.getJavaDoc;
import static org.onosproject.yangutils.utils.io.impl.YangIoUtils.insertDataIntoJavaFile;
import static org.onosproject.yangutils.utils.io.impl.YangIoUtils.partString;
import static org.onosproject.yangutils.utils.io.impl.YangIoUtils.trimAtLast;

/**
 * Representation of java file generator.
 */
public final class JavaFileGenerator {

    /**
     * Flag to check whether generated interface file need to extends any class.
     */
    private static boolean isExtendsList = false;

    /**
     * List of classes to be extended by generated interface file.
     */
    private static List<String> extendsList = new ArrayList<>();

    /**
     * Creates an instance of java file generator.
     */
    private JavaFileGenerator() {
    }

    /**
     * Returns true if extends list is not empty.
     *
     * @return true or false
     */
    public static boolean isExtendsList() {
        return isExtendsList;
    }

    /**
     * Sets the value of is extends list.
     *
     * @param isExtends true or false
     */
    public static void setIsExtendsList(boolean isExtends) {
        isExtendsList = isExtends;
    }

    /**
     * Returns list of extended classes.
     *
     * @return list of extended classes
     */
    public static List<String> getExtendsList() {
        return extendsList;
    }

    /**
     * Sets the list of extended classes.
     *
     * @param extendList list of extended classes
     */
    public static void setExtendsList(List<String> extendList) {
        extendsList = extendList;
    }

    /**
     * Returns generated interface file for current node.
     *
     * @param file file
     * @param imports imports for the file
     * @param curNode current YANG node
     * @param isAttrPresent if any attribute is present or not
     * @return interface file
     * @throws IOException when fails to write in file
     */
    public static File generateInterfaceFile(File file, List<String> imports, YangNode curNode, boolean isAttrPresent)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, INTERFACE_MASK, imports, path);

        if (isAttrPresent) {
            /**
             * Add getter methods to interface file.
             */
            try {
                /**
                 * Getter methods.
                 */
                insertDataIntoJavaFile(file, getDataFromTempFileHandle(GETTER_FOR_INTERFACE_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()));
            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while interface file generation");
            }
        }
        return file;
    }

    /**
     * Returns generated builder interface file for current node.
     *
     * @param file file
     * @param curNode current YANG node
     * @param isAttrPresent if any attribute is present or not
     * @return builder interface file
     * @throws IOException when fails to write in file
     */
    public static File generateBuilderInterfaceFile(File file, YangNode curNode, boolean isAttrPresent)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, BUILDER_INTERFACE_MASK, null, path);
        List<String> methods = new ArrayList<>();
        if (isAttrPresent) {
            try {
                /**
                 * Getter methods.
                 */
                methods.add(FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(GETTER_FOR_INTERFACE_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()));
                /**
                 * Setter methods.
                 */
                methods.add(NEW_LINE);
                methods.add(FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(SETTER_FOR_INTERFACE_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()));
            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while builder interface file generation");
            }
        }
        /**
         * Add build method to builder interface file.
         */
        methods.add(
                ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                        .addBuildMethodForInterface());

        /**
         * Add getters and setters in builder interface.
         */
        for (String method : methods) {
            insertDataIntoJavaFile(file, method);
        }

        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);
        return file;
    }

    /**
     * Returns generated builder class file for current node.
     *
     * @param file file
     * @param imports imports for the file
     * @param curNode current YANG node
     * @param isAttrPresent if any attribute is present or not
     * @return builder class file
     * @throws IOException when fails to write in file
     */
    public static File generateBuilderClassFile(File file, List<String> imports, YangNode curNode,
            boolean isAttrPresent)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, BUILDER_CLASS_MASK, imports, path);

        List<String> methods = new ArrayList<>();

        if (isAttrPresent) {
            /**
             * Add attribute strings.
             */
            try {
                insertDataIntoJavaFile(file,
                        NEW_LINE + FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(ATTRIBUTES_MASK,
                                ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                        .getBeanTempFiles()));
            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while builder class file generation");
            }

            try {
                /**
                 * Getter methods.
                 */
                methods.add(getDataFromTempFileHandle(GETTER_FOR_CLASS_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()));
                /**
                 * Setter methods.
                 */
                methods.add(getDataFromTempFileHandle(SETTER_FOR_CLASS_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()) +
                        NEW_LINE);
            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while builder class file generation");
            }
        } else {
            insertDataIntoJavaFile(file, NEW_LINE);
        }
        /**
         * Add default constructor and build method impl.
         */
        methods.add(((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().addBuildMethodImpl());
        methods.add(((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                .addDefaultConstructor(PUBLIC, BUILDER));

        /**
         * Add methods in builder class.
         */
        for (String method : methods) {
            insertDataIntoJavaFile(file, method);
        }
        return file;
    }

    /**
     * Returns generated impl class file for current node.
     *
     * @param file file
     * @param curNode current YANG node
     * @param isAttrPresent if any attribute is present or not
     * @return impl class file
     * @throws IOException when fails to write in file
     */
    public static File generateImplClassFile(File file, YangNode curNode, boolean isAttrPresent)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, IMPL_CLASS_MASK, null, path);

        List<String> methods = new ArrayList<>();
        if (isAttrPresent) {
            /**
             * Add attribute strings.
             */
            try {
                insertDataIntoJavaFile(file,
                        NEW_LINE + FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(ATTRIBUTES_MASK,
                                ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                        .getBeanTempFiles()));
            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while impl class file generation");
            }

            /**
             * Add attribute for augmented info's list.
             */
            if (isHasAugmentationExtended(getExtendsList())) {
                insertDataIntoJavaFile(file, getAugmentedInfoAttribute());
            }
            insertDataIntoJavaFile(file, NEW_LINE);
            try {
                /**
                 * Getter methods.
                 */
                methods.add(getDataFromTempFileHandle(GETTER_FOR_CLASS_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles()));

                /**
                 * Hash code method.
                 */
                methods.add(getHashCodeMethodClose(getHashCodeMethodOpen() + partString(
                        getDataFromTempFileHandle(HASH_CODE_IMPL_MASK,
                                ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                        .getBeanTempFiles()).replace(NEW_LINE, EMPTY_STRING))));
                /**
                 * Equals method.
                 */
                methods.add(getEqualsMethodClose(
                        getEqualsMethodOpen(className + IMPL) + getDataFromTempFileHandle(EQUALS_IMPL_MASK,
                                ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                        .getBeanTempFiles())));
                /**
                 * To string method.
                 */
                methods.add(getToStringMethodOpen() + getDataFromTempFileHandle(TO_STRING_IMPL_MASK,
                        ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                .getBeanTempFiles())
                        + getToStringMethodClose());

            } catch (IOException e) {
                throw new IOException("No data found in temporary java code fragment files for " + className
                        + " while impl class file generation");
            }
        } else {
            insertDataIntoJavaFile(file, NEW_LINE);
        }
        try {
            /**
             * Constructor.
             */
            methods.add(getConstructorStart(className) + getDataFromTempFileHandle(CONSTRUCTOR_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getBeanTempFiles())
                    + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET);
        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while impl class file generation");
        }

        /**
         * Add method for augment info's list.
         */
        if (isHasAugmentationExtended(getExtendsList())) {
            methods.add(getAddAugmentInfoMethodImpl());
            methods.add(getAugmentInfoListImpl());
            methods.add(getRemoveAugmentationImpl());
        }

        /**
         * Add methods in impl class.
         */
        for (String method : methods) {
            insertDataIntoJavaFile(file, FOUR_SPACE_INDENTATION + method + NEW_LINE);
        }
        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);

        return file;
    }

    /**
     * Generates class file for type def.
     *
     * @param file generated file
     * @param curNode current YANG node
     * @param imports imports for file
     * @return type def class file
     * @throws IOException when fails to generate class file
     */
    public static File generateTypeDefClassFile(File file, YangNode curNode, List<String> imports)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, GENERATE_TYPEDEF_CLASS, imports, path);

        List<String> methods = new ArrayList<>();

        /**
         * Add attribute strings.
         */
        try {
            insertDataIntoJavaFile(file,
                    NEW_LINE + FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(ATTRIBUTES_MASK,
                            ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                    .getTypeTempFiles()));
        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while type def class file generation");
        }

        /**
         * Default constructor.
         */
        methods.add(((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                .addDefaultConstructor(PRIVATE, EMPTY_STRING));

        try {

            /**
             * Type constructor.
             */
            methods.add(getDataFromTempFileHandle(CONSTRUCTOR_FOR_TYPE_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Of method.
             */
            methods.add(getDataFromTempFileHandle(OF_STRING_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Getter method.
             */
            methods.add(getDataFromTempFileHandle(GETTER_FOR_CLASS_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Hash code method.
             */
            methods.add(getHashCodeMethodClose(getHashCodeMethodOpen() + partString(
                    getDataFromTempFileHandle(HASH_CODE_IMPL_MASK,
                            ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                    .getTypeTempFiles())
                            .replace(NEW_LINE, EMPTY_STRING))));

            /**
             * Equals method.
             */
            methods.add(getEqualsMethodClose(getEqualsMethodOpen(className + EMPTY_STRING)
                    + getDataFromTempFileHandle(EQUALS_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles())));

            /**
             * To string method.
             */
            methods.add(getToStringMethodOpen() + getDataFromTempFileHandle(TO_STRING_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles())
                    + getToStringMethodClose());

            JavaCodeGeneratorInfo javaGeninfo = (JavaCodeGeneratorInfo) curNode;
            /**
             * From string method.
             */
            methods.add(getFromStringMethodSignature(className)
                    + getDataFromTempFileHandle(FROM_STRING_IMPL_MASK, javaGeninfo.getTempJavaCodeFragmentFiles()
                    .getTypeTempFiles()) + getFromStringMethodClose());

        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while type def class file generation");
        }

        for (String method : methods) {
            insertDataIntoJavaFile(file, method);
        }
        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);

        return file;
    }

    /**
     * Generates class file for union type.
     *
     * @param file generated file
     * @param curNode current YANG node
     * @param imports imports for file
     * @return type def class file
     * @throws IOException when fails to generate class file
     */
    public static File generateUnionClassFile(File file, YangNode curNode, List<String> imports)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, GENERATE_UNION_CLASS, imports, path);

        List<String> methods = new ArrayList<>();

        /**
         * Add attribute strings.
         */
        try {
            insertDataIntoJavaFile(file,
                    NEW_LINE + FOUR_SPACE_INDENTATION + getDataFromTempFileHandle(ATTRIBUTES_MASK,
                            ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                    .getTypeTempFiles()));
        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while union class file generation");
        }

        /**
         * Default constructor.
         */
        methods.add(((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                .addDefaultConstructor(PRIVATE, EMPTY_STRING));

        try {

            /**
             * Type constructor.
             */
            methods.add(getDataFromTempFileHandle(CONSTRUCTOR_FOR_TYPE_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Of string method.
             */
            methods.add(getDataFromTempFileHandle(OF_STRING_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Getter method.
             */
            methods.add(getDataFromTempFileHandle(GETTER_FOR_CLASS_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles()));

            /**
             * Hash code method.
             */
            methods.add(getHashCodeMethodClose(getHashCodeMethodOpen() + partString(
                    getDataFromTempFileHandle(HASH_CODE_IMPL_MASK,
                            ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                    .getTypeTempFiles())
                            .replace(NEW_LINE, EMPTY_STRING))));

            /**
             * Equals method.
             */
            methods.add(getEqualsMethodClose(getEqualsMethodOpen(className + EMPTY_STRING)
                    + getDataFromTempFileHandle(EQUALS_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles())));

            /**
             * To string method.
             */
            methods.add(getToStringMethodOpen() + getOmitNullValueString() +
                    getDataFromTempFileHandle(TO_STRING_IMPL_MASK,
                            ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles()
                                    .getTypeTempFiles()) + getToStringMethodClose());

            /**
             * From string method.
             */
            methods.add(getFromStringMethodSignature(className)
                    + getDataFromTempFileHandle(FROM_STRING_IMPL_MASK,
                    ((TempJavaCodeFragmentFilesContainer) curNode).getTempJavaCodeFragmentFiles().getTypeTempFiles())
                    + getFromStringMethodClose());

        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while union class file generation");
        }

        for (String method : methods) {
            insertDataIntoJavaFile(file, method);
        }
        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);

        return file;
    }

    /**
     * Generates class file for type enum.
     *
     * @param file generated file
     * @param curNode current YANG node
     * @return class file for type enum
     * @throws IOException when fails to generate class file
     */
    public static File generateEnumClassFile(File file, YangNode curNode)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName());
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, GENERATE_ENUM_CLASS, null, path);
        /**
         * Add attribute strings.
         */
        try {
            JavaCodeGeneratorInfo javaGeninfo = (JavaCodeGeneratorInfo) curNode;
            insertDataIntoJavaFile(file,
                    trimAtLast(trimAtLast(getDataFromTempFileHandle(ENUM_IMPL_MASK, javaGeninfo
                            .getTempJavaCodeFragmentFiles().getEnumerationTempFiles()), COMMA), NEW_LINE)
                            + SEMI_COLAN + NEW_LINE);
        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while enum class file generation");
        }

        /**
         * Add an
         * attribute to get the enum's values.
         */
        insertDataIntoJavaFile(file, getEnumsValueAttribute(className));

        /**
         * Add a constructor for enum.
         */
        insertDataIntoJavaFile(file,
                getJavaDoc(TYPE_CONSTRUCTOR, getSmallCase(className), false) + getEnumsConstrcutor(className)
                        + NEW_LINE);

        /**
         * Add a getter method for enum.
         */
        insertDataIntoJavaFile(file,
                getJavaDoc(GETTER_METHOD, getSmallCase(className), false) + getGetter(INT, getSmallCase(className))
                        + NEW_LINE);

        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);

        return file;
    }

    /**
     * Generates interface file for rpc.
     *
     * @param file generated file
     * @param curNode current YANG node
     * @param imports imports for file
     * @return type def class file
     * @throws IOException when fails to generate class file
     */
    public static File generateRpcInterfaceFile(File file, YangNode curNode, List<String> imports)
            throws IOException {

        JavaFileInfo javaFileInfo = ((JavaFileInfoContainer) curNode).getJavaFileInfo();

        String className = getCaptialCase(javaFileInfo.getJavaName()) + SERVICE_METHOD_STRING;
        String path = javaFileInfo.getBaseCodeGenPath() + javaFileInfo.getPackageFilePath();

        initiateJavaFileGeneration(file, className, GENERATE_RPC_INTERFACE, imports, path);

        List<String> methods = new ArrayList<>();

        try {

            JavaCodeGeneratorInfo javaGeninfo = (JavaCodeGeneratorInfo) curNode;
            /**
             * Rpc methods
             */
            methods.add(getDataFromTempFileHandle(RPC_IMPL_MASK, javaGeninfo.getTempJavaCodeFragmentFiles()
                    .getServiceTempFiles()));

        } catch (IOException e) {
            throw new IOException("No data found in temporary java code fragment files for " + className
                    + " while rpc class file generation");
        }

        for (String method : methods) {
            insertDataIntoJavaFile(file, method);
        }
        insertDataIntoJavaFile(file, CLOSE_CURLY_BRACKET + NEW_LINE);

        return file;
    }
}
