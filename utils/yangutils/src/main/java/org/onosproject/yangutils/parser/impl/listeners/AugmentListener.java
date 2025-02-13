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

import java.util.List;

import org.onosproject.yangutils.datamodel.CollisionDetector;
import org.onosproject.yangutils.datamodel.YangAugment;
import org.onosproject.yangutils.datamodel.YangModule;
import org.onosproject.yangutils.datamodel.YangNode;
import org.onosproject.yangutils.datamodel.YangNodeIdentifier;
import org.onosproject.yangutils.datamodel.YangSubModule;
import org.onosproject.yangutils.datamodel.YangUses;
import org.onosproject.yangutils.datamodel.exceptions.DataModelException;
import org.onosproject.yangutils.parser.Parsable;
import org.onosproject.yangutils.parser.antlrgencode.GeneratedYangParser;
import org.onosproject.yangutils.parser.exceptions.ParserException;
import org.onosproject.yangutils.parser.impl.TreeWalkListener;

import static org.onosproject.yangutils.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static org.onosproject.yangutils.datamodel.utils.YangDataModelFactory.getYangAugmentNode;
import static org.onosproject.yangutils.parser.impl.parserutils.AugmentJavaFileNameGenUtil.clearOccurrenceCount;
import static org.onosproject.yangutils.parser.impl.parserutils.AugmentJavaFileNameGenUtil.createValidNameForAugment;
import static org.onosproject.yangutils.parser.impl.parserutils.AugmentJavaFileNameGenUtil.updateNameWhenHasMultipleOuccrrence;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerCollisionDetector.detectCollidingChildUtil;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorLocation.ENTRY;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorLocation.EXIT;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorMessageConstruction.constructListenerErrorMessage;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.INVALID_HOLDER;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.MISSING_CURRENT_HOLDER;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.MISSING_HOLDER;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerErrorType.UNHANDLED_PARSED_DATA;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerUtil.getValidAbsoluteSchemaNodeId;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerValidation.checkStackIsNotEmpty;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerValidation.validateCardinalityMaxOne;
import static org.onosproject.yangutils.parser.impl.parserutils.ListenerValidation.validateMutuallyExclusiveChilds;
import static org.onosproject.yangutils.utils.YangConstructType.AUGMENT_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.CASE_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.DATA_DEF_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.DESCRIPTION_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.REFERENCE_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.STATUS_DATA;
import static org.onosproject.yangutils.utils.YangConstructType.WHEN_DATA;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  augment-stmt        = augment-keyword sep augment-arg-str optsep
 *                        "{" stmtsep
 *                            ;; these stmts can appear in any order
 *                            [when-stmt stmtsep]
 *                            *(if-feature-stmt stmtsep)
 *                            [status-stmt stmtsep]
 *                            [description-stmt stmtsep]
 *                            [reference-stmt stmtsep]
 *                            1*((data-def-stmt stmtsep) /
 *                               (case-stmt stmtsep))
 *                         "}"
 *
 * ANTLR grammar rule
 * augmentStatement : AUGMENT_KEYWORD augment LEFT_CURLY_BRACE (whenStatement | ifFeatureStatement | statusStatement
 *      | descriptionStatement | referenceStatement | dataDefStatement  | caseStatement)* RIGHT_CURLY_BRACE;
 */

/**
 * Represents listener based call back function corresponding to the "augment"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class AugmentListener {

    /**
     * Creates a new augment listener.
     */
    private AugmentListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (augment), performs validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx context object of the grammar rule
     */
    public static void processAugmentEntry(TreeWalkListener listener,
                                       GeneratedYangParser.AugmentStatementContext ctx) {

        // Check for stack to be non empty.
        checkStackIsNotEmpty(listener, MISSING_HOLDER, AUGMENT_DATA, ctx.augment().getText(), ENTRY);

        // Validate augment argument string
        List<YangNodeIdentifier> targetNodes = getValidAbsoluteSchemaNodeId(ctx.augment().getText(),
                AUGMENT_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        detectCollidingChildUtil(listener, line, charPositionInLine, "", AUGMENT_DATA);

        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangModule || curData instanceof YangSubModule || curData instanceof YangUses) {
            YangNode curNode = (YangNode) curData;
            YangAugment yangAugment = getYangAugmentNode(JAVA_GENERATION);
            yangAugment.setTargetNode(targetNodes);
            yangAugment.setName(detectCollisionForTargetNode(curData, targetNodes, line, charPositionInLine, listener));

            try {
                curNode.addChild(yangAugment);
            } catch (DataModelException e) {
                throw new ParserException(constructExtendedListenerErrorMessage(UNHANDLED_PARSED_DATA,
                        AUGMENT_DATA, ctx.augment().getText(), ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(yangAugment);
        } else {
            throw new ParserException(constructListenerErrorMessage(INVALID_HOLDER, AUGMENT_DATA,
                    ctx.augment().getText(), ENTRY));
        }

    }

    /**
     * It is called when parser exits from grammar rule (augment), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx context object of the grammar rule
     */
    public static void processAugmentExit(TreeWalkListener listener,
                                      GeneratedYangParser.AugmentStatementContext ctx) {

        //Check for stack to be non empty.
        checkStackIsNotEmpty(listener, MISSING_HOLDER, AUGMENT_DATA, ctx.augment().getText(), EXIT);

        if (!(listener.getParsedDataStack().peek() instanceof YangAugment)) {
            throw new ParserException(constructListenerErrorMessage(MISSING_CURRENT_HOLDER, AUGMENT_DATA,
                    ctx.augment().getText(), EXIT));
        }
        listener.getParsedDataStack().pop();
    }

    /**
     * Validates the cardinality of augment sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(GeneratedYangParser.AugmentStatementContext ctx) {
        validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, AUGMENT_DATA, ctx.augment().getText());
        validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, AUGMENT_DATA, ctx.augment().getText());
        validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, AUGMENT_DATA, ctx.augment().getText());
        validateCardinalityMaxOne(ctx.whenStatement(), WHEN_DATA, AUGMENT_DATA, ctx.augment().getText());
        validateMutuallyExclusiveChilds(ctx.dataDefStatement(), DATA_DEF_DATA, ctx.caseStatement(),
                                        CASE_DATA, AUGMENT_DATA, ctx.augment().getText());
    }

    /**
     * Detects collision for java file generation of augment node when
     * it is updating the same target node in same parent multiple times.
     * Returns name for generated java file of augment node
     *
     * @param curData parsable data
     * @param targetNodes list of target nodes
     * @param line line in YANG file
     * @param charPositionInLine char position in YANG file
     * @param listener tree walk listener
     * @return name for generated java file for augment node
     */
    private static String detectCollisionForTargetNode(Parsable curData, List<YangNodeIdentifier> targetNodes, int line,
            int charPositionInLine, TreeWalkListener listener) {

        String curPrefix = null;
        if (curData instanceof YangModule) {
            curPrefix = ((YangModule) curData).getPrefix();
        } else if (curData instanceof YangSubModule) {
            curPrefix = ((YangSubModule) curData).getPrefix();
        }
        YangNodeIdentifier nodeId = targetNodes.get(targetNodes.size() - 1);
        boolean isPrefix = isPrefixPresent(nodeId, curPrefix);
        String xpath = createValidNameForAugment(nodeId, isPrefix);

        if (listener.getParsedDataStack().peek() instanceof CollisionDetector) {
            try {
                ((CollisionDetector) listener.getParsedDataStack().peek()).detectCollidingChild(xpath,
                        AUGMENT_DATA);
            } catch (DataModelException e) {
                return updateNameWhenHasMultipleOuccrrence(nodeId, isPrefix);
            }
        }

        clearOccurrenceCount();
        return xpath;
    }

    /**
     * Returns true if a prefix is present and it is not equals to parents prefix.
     *
     * @param nodeId YANG node identifier
     * @param parentsPrefix parent's prefix
     * @return true if a prefix is present and it is not equals to parents prefix
     */
    private static boolean isPrefixPresent(YangNodeIdentifier nodeId, String parentsPrefix) {
        return nodeId.getPrefix() != null && nodeId.getPrefix() != parentsPrefix;
    }

    /**
     * Validates for the child nodes of augment node.
     */
    private static void validateForChildNodes() {
        //TODO: implement with linker.
    }
}
