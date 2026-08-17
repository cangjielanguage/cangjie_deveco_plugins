/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.impl;

import com.huawei.bitfun.intellij.ex.DapXValue;
import com.huawei.bitfun.intellij.treemodel.DapEvaluatedValueNode;
import com.huawei.bitfun.intellij.treemodel.DapStackFrameNode;
import com.huawei.bitfun.intellij.treemodel.DapTreeNode;
import com.huawei.bitfun.intellij.treemodel.DapVariableValueNode;
import com.huawei.bitfun.intellij.utils.EdtRequestCallbacks;
import com.huawei.bitfun.intellij.utils.FinishedEvaluation;
import com.huawei.bitfun.protocol.extend.EvaluateArguments;
import com.huawei.bitfun.protocol.extend.EvaluateResponse;
import com.huawei.bitfun.protocol.extend.Variable;
import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.EitherPair;
import com.huawei.bitfun.utils.ExceptionUtils;
import com.huawei.cangjie.debugger.impl.treemodel.CangjieVariableValueNode;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ThreeState;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.intellij.xdebugger.frame.XInlineDebuggerDataCallback;
import com.intellij.xdebugger.frame.XNavigatable;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.intellij.xdebugger.impl.ui.tree.nodes.HeadlessValueEvaluationCallback;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;

import org.eclipse.lsp4j.debug.DataBreakpointInfoResponse;
import org.eclipse.lsp4j.debug.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Cangjie XValue implementation
 *
 * @since 2022-10-17
 */

public class CangjieXValue extends DapXValue<CangjieXDebugProcess, CangjieVariableValueNode, DapEvaluatedValueNode> {
    private static final int DEFAULT_PAGE_SIZE = 30;

    private static final String EXT_EVALUATE_ARGS_VIEW_MORE = "isViewMore";

    private static final String VALUE_TOO_LONG_MARK = "...";

    private XSourcePosition cachedJumpToSourcePosition = null;

    private DapStackFrameNode stackFrameNode = null;

    private boolean isJumpToSourcePositionComputed = false;

    private boolean isMuted;

    /**
     * constructor
     *
     * @param nodePair   either variable node or evaluate node
     * @param dapProcess dapProcess
     */
    public CangjieXValue(EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> nodePair,
        CangjieXDebugProcess dapProcess) {
        super(nodePair, dapProcess);
    }

    @Override
    protected DapXValue<CangjieXDebugProcess, CangjieVariableValueNode, DapEvaluatedValueNode>
        createXValue(Variable variable) {
        CangjieVariableValueNode node = new CangjieVariableValueNode(variable, getTreeNode());
        EitherPair<CangjieVariableValueNode, DapEvaluatedValueNode> pair = EitherPair.left(node);
        return new CangjieXValue(pair, dapProcess);
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        if (dapProcess.getTimeTravelProcess().isOpenTimeTravel()) {
            super.computePresentation(node, place);
        } else if (isRegistersValue() || !isMuted) {
            super.computePresentation(node, place);
            if (value.endsWith(VALUE_TOO_LONG_MARK)) {
                node.setFullValueEvaluator(getViewMoreValueEvaluator());
            }
        } else {
            XRegularValuePresentation presentation = new XRegularValuePresentation("", type);
            node.setFullValueEvaluator(getMuteValueEvaluator());
            node.setPresentation(IconLoader.getIcon("/icons/valueMuted.svg", CangjieXValue.class), presentation, false);
        }
    }

    private XFullValueEvaluator getViewMoreValueEvaluator() {
        return new XFullValueEvaluator() {
            @Override
            public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
                viewMoreEvaluation(callback);
            }
        };
    }

    private void viewMoreEvaluation(@NotNull XFullValueEvaluator.XFullValueEvaluationCallback callback) {
        EvaluateArguments args = new EvaluateArguments();
        args.setExpression(evaluateName);
        args.setFrameId(getStackFrameNode().getModel().getId());
        args.getExtraFieldMap().put(EXT_EVALUATE_ARGS_VIEW_MORE, true);
        dapProcess.getToServerService()
                .getEvaluateRequester()
                .requestAsync(args, dapProcess.getTimeouts().evaluate(), new EdtRequestCallbacks<>() {
                    @Override
                    public void whenSuccess(EvaluateResponse evaluateResponse) {
                        callback.evaluated(evaluateResponse.getResult());
                    }

                    @Override
                    public void whenError(Exception ex) {
                        callback.errorOccurred(ExceptionUtils.getNonNullMsg(ex));
                    }
                });
    }

    private XFullValueEvaluator getMuteValueEvaluator() {
        XFullValueEvaluator xFullValueEvaluator = new XFullValueEvaluator("load") {
            @Override
            public void startEvaluation(@NotNull XFullValueEvaluationCallback callback) {
                if (!(callback instanceof HeadlessValueEvaluationCallback)) {
                    return;
                }
                XValueNodeImpl node = ((HeadlessValueEvaluationCallback) callback).getNode();
                node.clearFullValueEvaluator();
                isMuted = false;
                String expression = getEvaluationExpression();
                EvaluateArguments evaluateArguments = new EvaluateArguments();
                evaluateArguments.setFrameId(getStackFrameNode().getModel().getId());
                evaluateArguments.setExpression(expression);
                requestMuteValue(evaluateArguments, expression, node, callback);
            }
        };
        xFullValueEvaluator.setShowValuePopup(false);
        return xFullValueEvaluator;
    }

    private void requestMuteValue(EvaluateArguments evaluateArguments, String expression, XValueNodeImpl node,
                                  XFullValueEvaluator.XFullValueEvaluationCallback callback) {
        dapProcess.getToServerService().getEvaluateRequester().requestAsync(evaluateArguments,
                dapProcess.getTimeouts().evaluate(), new EdtRequestCallbacks<>() {
                    @Override
                    public void whenSuccessEdt(EvaluateResponse evaluateResponse) {
                        FinishedEvaluation evaluation = new FinishedEvaluation(expression, evaluateResponse);
                        DapEvaluatedValueNode evaluateNode =
                                new DapEvaluatedValueNode(evaluation, getStackFrameNode());
                        CangjieXValue xValue = new CangjieXValue(EitherPair.right(evaluateNode), dapProcess);
                        xValue.computePresentation(node, XValuePlace.TREE);
                        callback.evaluated(evaluateResponse.getResult());
                    }

                    @Override
                    public void whenErrorEdt(Exception ex) {
                        callback.errorOccurred(ExceptionUtils.getNonNullMsg(ex));
                    }
                });
    }

    @Override
    protected int getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    @Override
    public boolean canNavigateToSource() {
        if (isRegistersValue()) {
            return false;
        }
        if (nodePair.isRight()) {
            return false;
        }
        CangjieVariableValueNode variableValueNode = nodePair.getLeft();
        if (variableValueNode.getDeclaredLine() <= 0) {
            return false;
        }
        if (!isJumpToSourcePositionComputed) {
            cachedJumpToSourcePosition = ApplicationManager.getApplication()
                    .runReadAction((Computable<XSourcePosition>) () -> {
                        int declaredLine = variableValueNode.getDeclaredLine();
                        return getCangjieVariableSourcePosition(dapProcess.getSession(), declaredLine,
                                variableNameOrExpression, variableValueNode.getDeclaredSource());
                    });
            isJumpToSourcePositionComputed = true;
        }
        return cachedJumpToSourcePosition != null;
    }

    @Override
    public XValueModifier getModifier() {
        XValueModifier valueModifier = super.getModifier();
        if (nodePair.isLeft()) {
            CangjieVariableValueNode variableValueNode = nodePair.getLeft();
            EitherPair<DataBreakpointInfoResponse, String> cachedDataBpInfoResult =
                    variableValueNode.getCachedDataBpInfoResult();
            if (cachedDataBpInfoResult != null && cachedDataBpInfoResult.isRight()) {
                variableValueNode.setCachedDataBpInfoResult(null);
            }
        }
        if (valueModifier == null) {
            return CodeCheckByPassUtils.getNull();
        }
        return valueModifier;
    }

    @Override
    @NotNull
    public ThreeState computeInlineDebuggerData(@NotNull XInlineDebuggerDataCallback callback) {
        if (canNavigateToSource()) {
            callback.computed(cachedJumpToSourcePosition);
        }
        return ThreeState.YES;
    }

    /**
     * compute source position
     *
     * @param navigatable navigatable
     */
    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        navigatable.setSourcePosition(cachedJumpToSourcePosition);
    }

    private DapStackFrameNode getStackFrameNode() {
        if (stackFrameNode == null) {
            stackFrameNode = nodePair.map(DapVariableValueNode::getStackFrameNode, DapTreeNode::getParentNode);
        }
        return stackFrameNode;
    }

    /**
     * get cangjie variable source position
     *
     * @param debugSession   debug session
     * @param declaredLine   declared line
     * @param name           name
     * @param declaredSource declaredSource
     * @return source position
     */
    @Nullable
    public static XSourcePosition getCangjieVariableSourcePosition(final XDebugSession debugSession,
        final int declaredLine, final String name, @Nullable Source declaredSource) {
        if (declaredSource == null || declaredSource.getPath() == null) {
            XSourcePosition currentPosition = debugSession.getCurrentPosition();
            if (currentPosition == null) {
                return CodeCheckByPassUtils.getNull();
            }
            // Declare the line number according to the variable
            // Add validation to avoid situation in which the variable is
            // declared in other file rather than current file.
            // But how about the variable declared in other file? Server should tell client the declared file name with
            // declared line together. However, we only show variable declared in stopped file now.
            if (validateVariableSourcePositionByPSI(debugSession, name, declaredLine)) {
                VirtualFile currentFile = currentPosition.getFile();
                return XDebuggerUtil.getInstance().createPosition(currentFile, declaredLine - 1);
            }
            return CodeCheckByPassUtils.getNull();
        } else {
            String path = declaredSource.getPath();
            VirtualFile vf = VirtualFileManager.getInstance().findFileByNioPath(CodeCheckByPassUtils.getPath(path));
            if (vf == null) {
                return CodeCheckByPassUtils.getNull();
            }
            return XDebuggerUtil.getInstance().createPosition(vf, declaredLine - 1);
        }
    }

    private static boolean validateVariableSourcePositionByPSI(final XDebugSession debugSession, final String name,
                                                               int declaredLine) {
        final XSourcePosition position = debugSession.getCurrentPosition();
        if (position == null) {
            return false;
        }
        VirtualFile file = position.getFile();
        PsiManager psiManager = PsiManager.getInstance(debugSession.getProject());
        PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return false;
        }
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) {
            return false;
        }
        int startLineOffset = document.getLineStartOffset(declaredLine - 1);
        int endLineOffset = document.getLineEndOffset(declaredLine - 1);
        // hack for lldb bug. E.g.Global var "x" is named "::x"
        String target = name.startsWith("::") ? name.substring(2) : name;
        for (int tag = startLineOffset; tag <= endLineOffset; tag++) {
            PsiElement element = psiFile.findElementAt(tag);
            if (element != null && element.getText().equals(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CangjieXValue cangjieXValue = (CangjieXValue) obj;
        return Objects.equals(variableNameOrExpression, cangjieXValue.variableNameOrExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableNameOrExpression);
    }

    @Override
    public String toString() {
        return "CangjieXValue{" + "variable ='" + variableNameOrExpression + '\'' + '}';
    }
}