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

package org.onosproject.yangutils.parser.impl.listeners;

import org.onosproject.yangutils.datamodel.YangDataTypes;
import org.onosproject.yangutils.datamodel.YangDerivedInfo;
import org.onosproject.yangutils.datamodel.YangRangeRestriction;
import org.onosproject.yangutils.datamodel.YangStringRestriction;
import org.onosproject.yangutils.datamodel.YangType;
import org.onosproject.yangutils.parser.Parsable;
import org.onosproject.yangutils.parser.antlrgencode.GeneratedYangParser;
import org.onosproject.yangutils.parser.exceptions.ParserException;
import org.onosproject.yangutils.parser.impl.TreeWalkListener;
import org.onosproject.yangutils.utils.YangConstructType;

import static org.onosproject.yangutils.datamodel.YangDataTypes.DERIVED;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorLocation.ENTRY;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorMessageConstruction.constructListenerErrorMessage;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.INVALID_HOLDER;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.MISSING_HOLDER;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerValidation.checkStackIsNotEmpty;
import static org.onosproject.yangutils.utils.RestrictionResolver.processLengthRestriction;
import static org.onosproject.yangutils.utils.YangConstructType.LENGTH_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.TYPE_DATA;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  length-stmt         = length-keyword sep length-arg-str optsep
 *                        (";" /
 *                         "{" stmtsep
 *                             ;; these stmts can appear in any order
 *                             [error-message-stmt stmtsep]
 *                             [error-app-tag-stmt stmtsep]
 *                             [description-stmt stmtsep]
 *                             [reference-stmt stmtsep]
 *                          "}")
 *
 *
 * ANTLR grammar rule
 * lengthStatement : LENGTH_KEYWORD length
 *                 (STMTEND | LEFT_CURLY_BRACE commonStatements RIGHT_CURLY_BRACE);
 */

/**
 * Represents listener based call back function corresponding to the "length"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class LengthRestrictionListener {

    /**
     * Creates a new length restriction listener.
     */
    private LengthRestrictionListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar
     * rule (length), performs validation and updates the data model
     * tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processLengthRestrictionEntry(TreeWalkListener listener,
                                                     GeneratedYangParser.LengthStatementContext ctx) {

        // Check for stack to be non empty.
        checkStackIsNotEmpty(listener, MISSING_HOLDER, LENGTH_DATA, ctx.length().getText(), ENTRY);

        Parsable tmpData = listener.getParsedDataStack().peek();
        if (tmpData.getYangConstructType() == TYPE_DATA) {
            YangType type = (YangType) tmpData;
            setLengthRestriction(type, ctx);
        } else {
            throw new ParserException(constructListenerErrorMessage(INVALID_HOLDER, LENGTH_DATA,
                    ctx.length().getText(), ENTRY));
        }
    }

    /**
     * Sets the length restriction to type.
     *
     * @param type Yang type for which length restriction to be set
     * @param ctx  context object of the grammar rule
     */
    private static void setLengthRestriction(YangType type,
                                             GeneratedYangParser.LengthStatementContext ctx) {

        if (type.getDataType() == DERIVED) {
            ((YangDerivedInfo<YangRangeRestriction>) type.getDataTypeExtendedInfo())
                    .setLengthRestrictionString(ctx.length().getText());
            ((YangDerivedInfo<YangRangeRestriction>) type.getDataTypeExtendedInfo())
                    .setLineNumber(ctx.getStart().getLine());
            ((YangDerivedInfo<YangRangeRestriction>) type.getDataTypeExtendedInfo())
                    .setCharPosition(ctx.getStart().getCharPositionInLine());
            return;
        }

        if (type.getDataType() != YangDataTypes.STRING) {
            ParserException parserException = new ParserException("YANG file error : " +
                    YangConstructType.getYangConstructType(LENGTH_DATA) + " name " + ctx.length().getText() +
                    " can be used to restrict the built-in type string or types derived from string.");
            parserException.setLine(ctx.getStart().getLine());
            parserException.setCharPosition(ctx.getStart().getCharPositionInLine());
            throw parserException;
        }

        YangRangeRestriction lengthRestriction = processLengthRestriction(null, ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(), false, ctx.length().getText());

        YangStringRestriction stringRestriction = (YangStringRestriction) type.getDataTypeExtendedInfo();

        if (stringRestriction == null) {
            stringRestriction = new YangStringRestriction();
            type.setDataTypeExtendedInfo(stringRestriction);
        }

        stringRestriction.setLengthRestriction(lengthRestriction);
    }
}
