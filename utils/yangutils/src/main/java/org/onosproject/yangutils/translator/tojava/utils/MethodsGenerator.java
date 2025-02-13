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

import org.onosproject.yangutils.translator.tojava.JavaAttributeInfo;

import static org.onosproject.yangutils.translator.tojava.utils.AttributesJavaDataType.getParseFromStringMethod;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getCamelCase;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getCaptialCase;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getSmallCase;
import static org.onosproject.yangutils.utils.UtilConstants.ADD_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.AND;
import static org.onosproject.yangutils.utils.UtilConstants.AUGMENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.AUGMENTED_INFO;
import static org.onosproject.yangutils.utils.UtilConstants.BOOLEAN_DATA_TYPE;
import static org.onosproject.yangutils.utils.UtilConstants.BUILD;
import static org.onosproject.yangutils.utils.UtilConstants.BUILDER;
import static org.onosproject.yangutils.utils.UtilConstants.CATCH;
import static org.onosproject.yangutils.utils.UtilConstants.CHECK_NOT_NULL_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.CLEAR;
import static org.onosproject.yangutils.utils.UtilConstants.CLOSE_CURLY_BRACKET;
import static org.onosproject.yangutils.utils.UtilConstants.CLOSE_PARENTHESIS;
import static org.onosproject.yangutils.utils.UtilConstants.COMMA;
import static org.onosproject.yangutils.utils.UtilConstants.DIAMOND_CLOSE_BRACKET;
import static org.onosproject.yangutils.utils.UtilConstants.DIAMOND_OPEN_BRACKET;
import static org.onosproject.yangutils.utils.UtilConstants.EIGHT_SPACE_INDENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.EMPTY_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.EQUAL;
import static org.onosproject.yangutils.utils.UtilConstants.EQUALS_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.EXCEPTION;
import static org.onosproject.yangutils.utils.UtilConstants.EXCEPTION_VAR;
import static org.onosproject.yangutils.utils.UtilConstants.FALSE;
import static org.onosproject.yangutils.utils.UtilConstants.FOUR_SPACE_INDENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.FROM_STRING_METHOD_NAME;
import static org.onosproject.yangutils.utils.UtilConstants.FROM_STRING_PARAM_NAME;
import static org.onosproject.yangutils.utils.UtilConstants.GET_METHOD_PREFIX;
import static org.onosproject.yangutils.utils.UtilConstants.GOOGLE_MORE_OBJECT_METHOD_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.HASH;
import static org.onosproject.yangutils.utils.UtilConstants.HASH_CODE_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.IF;
import static org.onosproject.yangutils.utils.UtilConstants.IMPL;
import static org.onosproject.yangutils.utils.UtilConstants.INSTANCE_OF;
import static org.onosproject.yangutils.utils.UtilConstants.INT;
import static org.onosproject.yangutils.utils.UtilConstants.LIST;
import static org.onosproject.yangutils.utils.UtilConstants.NEW;
import static org.onosproject.yangutils.utils.UtilConstants.NEW_LINE;
import static org.onosproject.yangutils.utils.UtilConstants.NULL;
import static org.onosproject.yangutils.utils.UtilConstants.OBJ;
import static org.onosproject.yangutils.utils.UtilConstants.OBJECT;
import static org.onosproject.yangutils.utils.UtilConstants.OBJECT_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.OF;
import static org.onosproject.yangutils.utils.UtilConstants.OMIT_NULL_VALUE_STRING;
import static org.onosproject.yangutils.utils.UtilConstants.OPEN_CURLY_BRACKET;
import static org.onosproject.yangutils.utils.UtilConstants.OPEN_PARENTHESIS;
import static org.onosproject.yangutils.utils.UtilConstants.OTHER;
import static org.onosproject.yangutils.utils.UtilConstants.OVERRIDE;
import static org.onosproject.yangutils.utils.UtilConstants.PERIOD;
import static org.onosproject.yangutils.utils.UtilConstants.PUBLIC;
import static org.onosproject.yangutils.utils.UtilConstants.QUOTES;
import static org.onosproject.yangutils.utils.UtilConstants.RETURN;
import static org.onosproject.yangutils.utils.UtilConstants.RPC_INPUT_VAR_NAME;
import static org.onosproject.yangutils.utils.UtilConstants.SEMI_COLAN;
import static org.onosproject.yangutils.utils.UtilConstants.SET_METHOD_PREFIX;
import static org.onosproject.yangutils.utils.UtilConstants.SIXTEEN_SPACE_INDENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.SPACE;
import static org.onosproject.yangutils.utils.UtilConstants.STATIC;
import static org.onosproject.yangutils.utils.UtilConstants.STRING_DATA_TYPE;
import static org.onosproject.yangutils.utils.UtilConstants.SUFFIX_S;
import static org.onosproject.yangutils.utils.UtilConstants.THIS;
import static org.onosproject.yangutils.utils.UtilConstants.TMP_VAL;
import static org.onosproject.yangutils.utils.UtilConstants.TO;
import static org.onosproject.yangutils.utils.UtilConstants.TRUE;
import static org.onosproject.yangutils.utils.UtilConstants.TRY;
import static org.onosproject.yangutils.utils.UtilConstants.TWELVE_SPACE_INDENTATION;
import static org.onosproject.yangutils.utils.UtilConstants.VALUE;
import static org.onosproject.yangutils.utils.UtilConstants.VOID;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.BUILD_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.CONSTRUCTOR;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.DEFAULT_CONSTRUCTOR;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.FROM_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.GETTER_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.OF_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.SETTER_METHOD;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.JavaDocType.TYPE_CONSTRUCTOR;
import static org.onosproject.yangutils.utils.io.impl.JavaDocGen.getJavaDoc;
import static org.onosproject.yangutils.utils.io.impl.YangIoUtils.trimAtLast;

/**
 * Represents generator for methods of generated files based on the file type.
 */
public final class MethodsGenerator {

    /**
     * Creates an instance of method generator.
     */
    private MethodsGenerator() {
    }

    /**
     * Returns the methods strings for builder interface.
     *
     * @param name attribute name
     * @return method string for builder interface
     */
    public static String parseBuilderInterfaceBuildMethodString(String name) {
        return getJavaDoc(BUILD_METHOD, name, false) + getBuildForInterface(name);
    }

    /**
     * Returns getter string.
     *
     * @param attr attribute info
     * @return getter string
     */
    public static String getGetterString(JavaAttributeInfo attr) {

        String returnType = getReturnType(attr);
        String attributeName = getSmallCase(attr.getAttributeName());

        return getJavaDoc(GETTER_METHOD, attributeName, attr.isListAttr())
                + getGetterForInterface(attributeName, returnType, attr.isListAttr());
    }

    /**
     * Returns setter string.
     *
     * @param attr attribute info
     * @param className java class name
     * @return setter string
     */
    public static String getSetterString(JavaAttributeInfo attr, String className) {

        String attrType = getReturnType(attr);
        String attributeName = getSmallCase(attr.getAttributeName());

        return getJavaDoc(SETTER_METHOD, attributeName, attr.isListAttr())
                + getSetterForInterface(attributeName, attrType, className, attr.isListAttr());
    }

    /**
     * Returns constructor method string.
     *
     * @param name class name
     * @return constructor string
     */
    public static String getConstructorString(String name) {
        return getJavaDoc(CONSTRUCTOR, name, false);
    }

    /**
     * Returns default constructor method string.
     *
     * @param name class name
     * @param modifierType modifier type
     * @return default constructor string
     */
    public static String getDefaultConstructorString(String name, String modifierType) {
        return getJavaDoc(DEFAULT_CONSTRUCTOR, name, false) + getDefaultConstructor(name, modifierType)
                + NEW_LINE;
    }

    /**
     * Returns check not null string.
     *
     * @param name attribute name
     * @return check not null string
     */
    public static String getCheckNotNull(String name) {
        return EIGHT_SPACE_INDENTATION + CHECK_NOT_NULL_STRING + OPEN_PARENTHESIS + name + COMMA + SPACE + name
                + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE;
    }

    /**
     * Returns build method string.
     *
     * @param name class name
     * @return build string
     */
    public static String getBuildString(String name) {
        return FOUR_SPACE_INDENTATION + OVERRIDE + NEW_LINE + getBuild(name);
    }

    /**
     * Returns the getter method strings for class file.
     *
     * @param attr attribute info
     * @return getter method for class
     */
    public static String getGetterForClass(JavaAttributeInfo attr) {

        String attrQuaifiedType = getReturnType(attr);
        String attributeName = getSmallCase(attr.getAttributeName());

        if (!attr.isListAttr()) {
            return getGetter(attrQuaifiedType, attributeName);
        }
        String listAttr = getListString() + attrQuaifiedType + DIAMOND_CLOSE_BRACKET;
        return getGetter(listAttr, attributeName);
    }

    /**
     * Returns getter for attribute.
     *
     * @param type return type
     * @param name attribute name
     * @return getter for attribute
     */
    public static String getGetter(String type, String name) {
        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + type + SPACE + GET_METHOD_PREFIX + getCaptialCase(name)
                + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION
                + RETURN + SPACE + name + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns the setter method strings for class file.
     *
     * @param attr attribute info
     * @param className name of the class
     * @return setter method for class
     */
    public static String getSetterForClass(JavaAttributeInfo attr, String className) {

        String attrQuaifiedType = getReturnType(attr);
        String attributeName = getSmallCase(attr.getAttributeName());
        if (!attr.isListAttr()) {
            return getSetter(className, attributeName, attrQuaifiedType);
        }
        String listAttr = getListString() + attrQuaifiedType + DIAMOND_CLOSE_BRACKET;
        return getSetter(className, attributeName, listAttr);
    }

    /**
     * Returns setter for attribute.
     *
     * @param className class name
     * @param name attribute name
     * @param type return type
     * @return setter for attribute
     */
    private static String getSetter(String className, String name, String type) {
        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + className + BUILDER + SPACE + SET_METHOD_PREFIX
                + getCaptialCase(name) + OPEN_PARENTHESIS + type + SPACE + name + CLOSE_PARENTHESIS + SPACE
                + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION + THIS + PERIOD + name + SPACE + EQUAL + SPACE
                + name + SEMI_COLAN + NEW_LINE + EIGHT_SPACE_INDENTATION + RETURN + SPACE + THIS + SEMI_COLAN
                + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns the setter method strings for class file.
     *
     * @param attr attribute info
     * @return setter method for class
     */
    public static String getSetterForTypeDefClass(JavaAttributeInfo attr) {

        String attrQuaifiedType = getReturnType(attr);
        String attributeName = getSmallCase(attr.getAttributeName());
        return getTypeDefSetter(attrQuaifiedType, attributeName);
    }

    /**
     * Returns type def's setter for attribute.
     *
     * @param type data type
     * @param name attribute name
     * @return setter for type def's attribute
     */
    private static String getTypeDefSetter(String type, String name) {
        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + VOID + SPACE + SET_METHOD_PREFIX + getCaptialCase(name)
                + OPEN_PARENTHESIS + type + SPACE + VALUE + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE
                + EIGHT_SPACE_INDENTATION + THIS + PERIOD + name + SPACE + EQUAL + SPACE + VALUE + SEMI_COLAN + NEW_LINE
                + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns override string.
     *
     * @return override string
     */
    public static String getOverRideString() {
        return NEW_LINE + FOUR_SPACE_INDENTATION + OVERRIDE + NEW_LINE;
    }

    /**
     * Returns the getter method strings for interface file.
     *
     * @param yangName name of the attribute
     * @param returnType return type of attribute
     * @param isList is list attribute
     * @return getter method for interface
     */
    public static String getGetterForInterface(String yangName, String returnType, boolean isList) {

        if (!isList) {
            return getGetterInterfaceString(returnType, yangName);
        }
        String listAttr = getListString() + returnType + DIAMOND_CLOSE_BRACKET;
        return getGetterInterfaceString(listAttr, yangName);
    }

    /**
     * Returns getter for attribute in interface.
     *
     * @param returnType return type
     * @param yangName attribute name
     * @return getter for interface
     */
    private static String getGetterInterfaceString(String returnType, String yangName) {
        return FOUR_SPACE_INDENTATION + returnType + SPACE + GET_METHOD_PREFIX + getCaptialCase(yangName)
                + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SEMI_COLAN;
    }

    /**
     * Returns the setter method strings for interface file.
     *
     * @param attrName name of the attribute
     * @param attrType return type of attribute
     * @param className name of the java class being generated
     * @param isList is list attribute
     * @return setter method for interface
     */
    public static String getSetterForInterface(String attrName, String attrType, String className, boolean isList) {

        if (!isList) {
            return getSetterInterfaceString(className, attrName, attrType);
        }
        String listAttr = getListString() + attrType + DIAMOND_CLOSE_BRACKET;
        return getSetterInterfaceString(className, attrName, listAttr);
    }

    /**
     * Returns setter string for interface.
     *
     * @param className class name
     * @param attrName attribute name
     * @param attrType attribute type
     * @return setter string
     */
    private static String getSetterInterfaceString(String className, String attrName, String attrType) {
        return FOUR_SPACE_INDENTATION + className + BUILDER + SPACE + SET_METHOD_PREFIX + getCaptialCase(attrName)
                + OPEN_PARENTHESIS + attrType + SPACE + attrName + CLOSE_PARENTHESIS + SEMI_COLAN;
    }

    /**
     * Returns list string.
     *
     * @return list string
     */
    private static String getListString() {
        return LIST + DIAMOND_OPEN_BRACKET;
    }

    /**
     * Returns return type for attribute.
     *
     * @param attr attribute info
     * @return return type
     */
    private static String getReturnType(JavaAttributeInfo attr) {

        String returnType = EMPTY_STRING;
        if (attr.isQualifiedName() && attr.getImportInfo().getPkgInfo() != null) {
            returnType = attr.getImportInfo().getPkgInfo() + PERIOD;
        }
        returnType = returnType + attr.getImportInfo().getClassInfo();
        return returnType;
    }

    /**
     * Returns the build method strings for interface file.
     *
     * @param yangName name of the interface
     * @return build method for interface
     */
    public static String getBuildForInterface(String yangName) {
        return FOUR_SPACE_INDENTATION + yangName + SPACE + BUILD + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SEMI_COLAN
                + NEW_LINE;
    }

    /**
     * Returns constructor string for impl class.
     *
     * @param yangName class name
     * @return constructor string
     */
    public static String getConstructorStart(String yangName) {

        String javadoc = getConstructorString(yangName);
        String constructor = FOUR_SPACE_INDENTATION + PUBLIC + SPACE + yangName + IMPL + OPEN_PARENTHESIS + yangName
                + BUILDER + SPACE + BUILDER.toLowerCase() + OBJECT + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET
                + NEW_LINE;
        return javadoc + constructor;
    }

    /**
     * Returns the constructor strings for class file.
     *
     * @param yangName name of the class
     * @param attr attribute info
     * @return constructor for class
     */
    public static String getConstructor(String yangName, JavaAttributeInfo attr) {

        String attributeName = getSmallCase(attr.getAttributeName());

        String constructor = EIGHT_SPACE_INDENTATION + THIS + PERIOD + getCamelCase(attributeName, null) + SPACE + EQUAL
                + SPACE + BUILDER.toLowerCase() + OBJECT + PERIOD + GET_METHOD_PREFIX
                + getCaptialCase(getCamelCase(attributeName, null)) + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SEMI_COLAN
                + NEW_LINE;

        return constructor;
    }

    /**
     * Returns the rpc strings for service interface.
     *
     * @param rpcName name of the rpc
     * @param inputName name of input
     * @param outputName name of output
     * @return rpc method string
     */
    public static String getRpcStringMethod(String rpcName, String inputName, String outputName) {

        rpcName = getSmallCase(getCamelCase(rpcName, null));
        inputName = getCaptialCase(inputName);
        outputName = getCaptialCase(outputName);

        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + outputName + SPACE + rpcName + OPEN_PARENTHESIS
                + inputName + SPACE + RPC_INPUT_VAR_NAME + CLOSE_PARENTHESIS + SEMI_COLAN;
    }

    /**
     * Returns the build method strings for class file.
     *
     * @param yangName class name
     * @return build method string for class
     */
    public static String getBuild(String yangName) {
        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + yangName + SPACE + BUILD + OPEN_PARENTHESIS + CLOSE_PARENTHESIS
                + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION + RETURN + SPACE + NEW + SPACE
                + yangName + IMPL + OPEN_PARENTHESIS + THIS + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE
                + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns the Default constructor strings for class file.
     *
     * @param name name of the class
     * @param modifierType modifier type for default constructor
     * @return Default constructor for class
     */
    private static String getDefaultConstructor(String name, String modifierType) {
        return FOUR_SPACE_INDENTATION + modifierType + SPACE + name + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE
                + OPEN_CURLY_BRACKET + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns to string method's open strings.
     *
     * @return string method's open string
     */
    public static String getToStringMethodOpen() {
        return getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + STRING_DATA_TYPE + SPACE + TO
                + STRING_DATA_TYPE + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE
                + EIGHT_SPACE_INDENTATION + RETURN + GOOGLE_MORE_OBJECT_METHOD_STRING + NEW_LINE;
    }

    /**
     * Returns omit null value string.
     *
     * @return omit null value string
     */
    public static String getOmitNullValueString() {
        return TWELVE_SPACE_INDENTATION + PERIOD + OMIT_NULL_VALUE_STRING + NEW_LINE;
    }

    /**
     * Returns to string method's close string.
     *
     * @return to string method close string
     */
    public static String getToStringMethodClose() {
        return TWELVE_SPACE_INDENTATION + PERIOD + TO + STRING_DATA_TYPE + OPEN_PARENTHESIS + CLOSE_PARENTHESIS
                + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET + NEW_LINE;
    }

    /**
     * Returns to string method for class.
     *
     * @param attr attribute info
     * @return to string method
     */
    public static String getToStringMethod(JavaAttributeInfo attr) {

        String attributeName = getSmallCase(attr.getAttributeName());
        return TWELVE_SPACE_INDENTATION + PERIOD + ADD_STRING + OPEN_PARENTHESIS + QUOTES + attributeName + QUOTES
                + COMMA + SPACE + attributeName + CLOSE_PARENTHESIS;
    }

    /**
     * Returns from string method's open string.
     *
     * @param className name of the class
     * @return from string method's open string
     */
    public static String getFromStringMethodSignature(String className) {
        return getJavaDoc(FROM_METHOD, className, false) + FOUR_SPACE_INDENTATION + PUBLIC + SPACE
                + className + SPACE + FROM_STRING_METHOD_NAME + OPEN_PARENTHESIS + STRING_DATA_TYPE + SPACE
                + FROM_STRING_PARAM_NAME + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE;
    }

    /**
     * Return from string method's close string.
     *
     * @return from string method's close string
     */
    public static String getFromStringMethodClose() {
        return EIGHT_SPACE_INDENTATION + RETURN + SPACE + NULL + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION +
                CLOSE_CURLY_BRACKET + NEW_LINE;
    }

    /**
     * Return from string method's body string.
     *
     * @param attr attribute info
     * @param fromStringAttributeInfo attribute info for the from string
     * wrapper type
     * @return from string method's body string
     */
    public static String getFromStringMethod(JavaAttributeInfo attr,
            JavaAttributeInfo fromStringAttributeInfo) {

        return EIGHT_SPACE_INDENTATION + getTrySubString() + NEW_LINE + TWELVE_SPACE_INDENTATION
                + getParsedSubString(attr, fromStringAttributeInfo) + SEMI_COLAN + NEW_LINE + TWELVE_SPACE_INDENTATION
                + getReturnOfSubString() + NEW_LINE + EIGHT_SPACE_INDENTATION + getCatchSubString()
                + NEW_LINE + EIGHT_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns sub string with try statement for union's from string method.
     *
     * @return sub string with try statement for union's from string method
     */
    public static String getTrySubString() {
        return TRY + SPACE + OPEN_CURLY_BRACKET;
    }

    /**
     * Returns sub string with return statement for union's from string method.
     *
     * @return sub string with return statement for union's from string method
     */
    public static String getReturnOfSubString() {
        return RETURN + SPACE + OF + OPEN_PARENTHESIS + TMP_VAL + CLOSE_PARENTHESIS + SEMI_COLAN;
    }

    /**
     * Returns sub string with catch statement for union's from string method.
     *
     * @return sub string with catch statement for union's from string method
     */
    public static String getCatchSubString() {
        return CLOSE_CURLY_BRACKET + SPACE + CATCH + SPACE + OPEN_PARENTHESIS + EXCEPTION + SPACE + EXCEPTION_VAR
                + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET;
    }

    /**
     * Returns sub string with parsed statement for union's from string method.
     *
     * @param attr attribute info
     * @return sub string with parsed statement for union's from string method
     */
    private static String getParsedSubString(JavaAttributeInfo attr,
            JavaAttributeInfo fromStringAttributeInfo) {

        String targetDataType = getReturnType(attr);
        String parseFromStringMethod = getParseFromStringMethod(targetDataType,
                fromStringAttributeInfo.getAttributeType());
        return targetDataType + SPACE + TMP_VAL + SPACE + EQUAL + SPACE + parseFromStringMethod
                + OPEN_PARENTHESIS + FROM_STRING_PARAM_NAME + CLOSE_PARENTHESIS;
    }


    /**
     * Returns hash code method open strings.
     *
     * @return hash code method open string
     */
    public static String getHashCodeMethodOpen() {
        return getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + INT + SPACE + HASH_CODE_STRING
                + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION
                + RETURN + SPACE + OBJECT_STRING + SUFFIX_S + PERIOD + HASH + OPEN_PARENTHESIS;
    }

    /**
     * Returns hash code methods close string.
     *
     * @param hashcodeString hash code string
     * @return to hash code method close string
     */
    public static String getHashCodeMethodClose(String hashcodeString) {
        hashcodeString = trimAtLast(hashcodeString, COMMA);
        hashcodeString = trimAtLast(hashcodeString, SPACE);
        return hashcodeString + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET
                + NEW_LINE;
    }

    /**
     * Returns hash code method for class.
     *
     * @param attr attribute info
     * @return hash code method
     */
    public static String getHashCodeMethod(JavaAttributeInfo attr) {
        return getSmallCase(attr.getAttributeName()) + COMMA + SPACE;
    }

    /**
     * Returns equals method open strings.
     *
     * @param className class name
     * @return equals method open string
     */
    public static String getEqualsMethodOpen(String className) {
        return getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + BOOLEAN_DATA_TYPE + SPACE + EQUALS_STRING
                + OPEN_PARENTHESIS + OBJECT_STRING + SPACE + OBJ + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET
                + NEW_LINE + getEqualsMethodsCommonIfCondition() + getEqualsMethodsSpecificIfCondition(className);
    }

    /**
     * Returns equal methods if condition string.
     *
     * @return if condition string
     */
    private static String getEqualsMethodsCommonIfCondition() {
        return EIGHT_SPACE_INDENTATION + IF + SPACE + OPEN_PARENTHESIS + THIS + SPACE + EQUAL + EQUAL + SPACE + OBJ
                + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + TWELVE_SPACE_INDENTATION + RETURN + SPACE
                + TRUE + SEMI_COLAN + NEW_LINE + EIGHT_SPACE_INDENTATION + CLOSE_CURLY_BRACKET + NEW_LINE;
    }

    /**
     * Returns if condition for specific class object in equals method.
     *
     * @param className class name
     * @return if condition string
     */
    private static String getEqualsMethodsSpecificIfCondition(String className) {
        return EIGHT_SPACE_INDENTATION + IF + SPACE + OPEN_PARENTHESIS + OBJ + INSTANCE_OF + className
                + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + TWELVE_SPACE_INDENTATION + className
                + SPACE + OTHER + SPACE + EQUAL + SPACE + OPEN_PARENTHESIS + className + CLOSE_PARENTHESIS + SPACE
                + OBJ + SEMI_COLAN + NEW_LINE + TWELVE_SPACE_INDENTATION + RETURN + NEW_LINE;
    }

    /**
     * Returns equals methods close string.
     *
     * @param equalMethodString equal method string
     * @return equals method close string
     */
    public static String getEqualsMethodClose(String equalMethodString) {
        equalMethodString = trimAtLast(equalMethodString, AND);
        equalMethodString = trimAtLast(equalMethodString, AND);
        equalMethodString = trimAtLast(equalMethodString, SPACE);
        equalMethodString = trimAtLast(equalMethodString, NEW_LINE) + SEMI_COLAN + NEW_LINE;
        return equalMethodString + EIGHT_SPACE_INDENTATION + CLOSE_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION
                + RETURN + SPACE + FALSE + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET
                + NEW_LINE;
    }

    /**
     * Returns equals method for class.
     *
     * @param attr attribute info
     * @return equals method
     */
    public static String getEqualsMethod(JavaAttributeInfo attr) {

        String attributeName = getSmallCase(attr.getAttributeName());
        return SIXTEEN_SPACE_INDENTATION + SPACE + OBJECT_STRING + SUFFIX_S + PERIOD + EQUALS_STRING + OPEN_PARENTHESIS
                + attributeName + COMMA + SPACE + OTHER + PERIOD + attributeName + CLOSE_PARENTHESIS + SPACE + AND
                + AND;
    }

    /**
     * Returns of method string for class.
     *
     * @param name class name
     * @param attr attribute info
     * @return of method string
     */
    public static String getOfMethod(String name, JavaAttributeInfo attr) {

        String attrQualifiedType = getReturnType(attr);

        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + STATIC + SPACE + name + SPACE + OF + OPEN_PARENTHESIS
                + attrQualifiedType + SPACE + VALUE + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE
                + EIGHT_SPACE_INDENTATION + RETURN + SPACE + NEW + SPACE + name + OPEN_PARENTHESIS + VALUE
                + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET + NEW_LINE;
    }

    /**
     * Returns implementation of add augmentation method of HasAugmentation class.
     *
     * @return implementation of add augmentation method of HasAugmentation class
     */
    public static String getAddAugmentInfoMethodImpl() {
        String method = FOUR_SPACE_INDENTATION;
        method = method + getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + VOID + SPACE + ADD_STRING
                + AUGMENTATION + OPEN_PARENTHESIS + AUGMENTED_INFO + SPACE + VALUE + CLOSE_PARENTHESIS + SPACE
                + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION + GET_METHOD_PREFIX + AUGMENTED_INFO + LIST
                + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + PERIOD + ADD_STRING + OPEN_PARENTHESIS + VALUE
                + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;

        return method;
    }

    /**
     * Returns of method's string and java doc for special type.
     *
     * @param attr attribute info
     * @param generatedJavaClassName class name
     * @return of method's string and java doc for special type
     */
    public static String getOfMethodStringAndJavaDoc(JavaAttributeInfo attr, String generatedJavaClassName) {

        String attrType = getReturnType(attr);
        String attrName = getSmallCase(attr.getAttributeName());

        return getJavaDoc(OF_METHOD, generatedJavaClassName + " for type " + attrName, false)
                + getOfMethodString(attrType, generatedJavaClassName);
    }

    /**
     * Returns of method's string.
     *
     * @param type data type
     * @param className class name
     * @return of method's string
     */
    private static String getOfMethodString(String type, String className) {

        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + STATIC + SPACE + className + SPACE + OF + OPEN_PARENTHESIS
                + type + SPACE + VALUE + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE
                + EIGHT_SPACE_INDENTATION + RETURN + SPACE + NEW + SPACE + className + OPEN_PARENTHESIS + VALUE
                + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns string and java doc for constructor of type class.
     *
     * @param attr attribute info
     * @param generatedJavaClassName class name
     * @return string and java doc for constructor of type class
     */
    public static String getTypeConstructorStringAndJavaDoc(JavaAttributeInfo attr, String generatedJavaClassName) {

        String attrType = getReturnType(attr);
        String attrName = getSmallCase(attr.getAttributeName());

        return getJavaDoc(TYPE_CONSTRUCTOR, generatedJavaClassName + " for type " + attrName, false)
                + getTypeConstructorString(attrType, attrName, generatedJavaClassName);
    }

    /**
     * Returns type constructor string.
     *
     * @param type data type
     * @param name attribute name
     * @param className class name
     * @return type constructor string
     */
    private static String getTypeConstructorString(String type, String name, String className) {

        return FOUR_SPACE_INDENTATION + PUBLIC + SPACE + className + OPEN_PARENTHESIS + type + SPACE + VALUE
                + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION + THIS + PERIOD
                + name + SPACE + EQUAL + SPACE + VALUE + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION
                + CLOSE_CURLY_BRACKET;
    }

    /**
     * Returns implementation of get augment info list method of HasAugmentation class.
     *
     * @return implementation of get augment info list method of HasAugmentation class
     */
    public static String getAugmentInfoListImpl() {

        String method = FOUR_SPACE_INDENTATION;
        method = method + getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + LIST + DIAMOND_OPEN_BRACKET
                + AUGMENTED_INFO + DIAMOND_CLOSE_BRACKET + SPACE + GET_METHOD_PREFIX + AUGMENTED_INFO + LIST
                + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION
                + RETURN + SPACE + getSmallCase(AUGMENTED_INFO) + LIST + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION
                + CLOSE_CURLY_BRACKET;
        return method;
    }

    /**
     * Returns implementation of remove augmentation method of HasAugmentation class.
     *
     * @return implementation of remove augmentation method of HasAugmentation class
     */
    public static String getRemoveAugmentationImpl() {
        String method = FOUR_SPACE_INDENTATION;
        method = method + getOverRideString() + FOUR_SPACE_INDENTATION + PUBLIC + SPACE + VOID + SPACE + "remove"
                + AUGMENTATION + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SPACE + OPEN_CURLY_BRACKET + NEW_LINE
                + EIGHT_SPACE_INDENTATION + GET_METHOD_PREFIX + AUGMENTED_INFO + LIST + OPEN_PARENTHESIS
                + CLOSE_PARENTHESIS + PERIOD + CLEAR + OPEN_PARENTHESIS + CLOSE_PARENTHESIS + SEMI_COLAN + NEW_LINE
                + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
        return method;
    }

    /**
     * Returns enum's constructor.
     *
     * @param className enum's class name
     * @return enum's constructor
     */
    public static String getEnumsConstrcutor(String className) {
        return FOUR_SPACE_INDENTATION + className + OPEN_PARENTHESIS + INT + SPACE + VALUE + CLOSE_PARENTHESIS + SPACE
                + OPEN_CURLY_BRACKET + NEW_LINE + EIGHT_SPACE_INDENTATION + getSmallCase(className) + SPACE + EQUAL
                + SPACE + VALUE + SEMI_COLAN + NEW_LINE + FOUR_SPACE_INDENTATION + CLOSE_CURLY_BRACKET;
    }
}
