/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.console;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;
import com.huawei.bitfun.utils.ThreadUtils;
import com.huawei.cangjie.debugger.deveco.lsp.LspUtils;

import com.google.gson.stream.JsonReader;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Instruction Completion Utils, Use InstructionCompletionUtils#INSTANCE to do completion
 *
 * @since 2022-10-19
 */
public class CommandCompletion {
    /**
     * INSTANCE
     */
    public static final CommandCompletion INSTANCE = new CommandCompletion();

    private final HashSet<String> commandsSet = new HashSet<>();

    private final Collection<LineExtensionInfo> lineExtensionInfos = new ArrayList<>();

    private CommandCompletion() {
        try(InputStream commandJsonStream = CommandCompletion.class.getClassLoader()
                .getResourceAsStream("lldbCommand/command.json")) {
            if (commandJsonStream == null) {
                // to_do print logger lldbCommand json not found!
                CodeCheckByPassUtils.doNothing();
            } else {
                parseCommandsFromJsonStream(commandJsonStream);
            }
        } catch (IOException e) {
            // to_do print file to stream exception
            CodeCheckByPassUtils.doNothing();
        }
    }

    private void parseCommandsFromJsonStream(InputStream commandJsonStream) {
        try (JsonReader reader = new JsonReader(
                new InputStreamReader(commandJsonStream, StandardCharsets.UTF_8))) {
            reader.beginArray();
            while (reader.hasNext()) {
                String command = reader.nextString();
                commandsSet.add(command);
            }
            reader.endArray();
        } catch (IOException e) {
            // to_do print stream to json exception
            CodeCheckByPassUtils.doNothing();
        }
    }

    /**
     * Complete user input
     *
     * @param editor editor to do complete
     */
    public void doCompletion(Editor editor) {
        final Document document = editor.getDocument();
        if (document.getText().trim().isEmpty()) {
            clearLineExtensionInfos();
            return;
        }
        ThreadUtils.createCachedThreadExecutor("command Completion").execute(() -> {
            CaretModel caretModel = editor.getCaretModel();
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                TextAttributes attributes = editor.getColorsScheme().getAttributes(
                        TextAttributesKey.find(LspUtils.LSP_LINE_COMMENT_ATTRS_KEY));
                String completeTarget = findTargetCommand(findPrefix(editor), attributes);
                int offset = caretModel.getOffset();
                document.insertString(offset, completeTarget);
                caretModel.moveToOffset(offset + completeTarget.length());
            });
        });
    }

    public Collection<LineExtensionInfo> getLineExtensionInfos() {
        return lineExtensionInfos;
    }

    /**
     * clear LineExtensionInfos which will be shown in console
     */
    public void clearLineExtensionInfos() {
        lineExtensionInfos.clear();
    }

    /**
     * Complete the prompt information according to the end of the input command
     *
     * @param editor editor
     * @return java.lang.String
     */
    private String findPrefix(Editor editor) {
        final Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        // delete space in the end of document
        String command = document.getText().substring(0, offset).replaceAll("\\s+$", "");
        document.deleteString(command.length(), offset);
        // Prefix the command prompt with the last word at the end of the command
        String[] commands = command.split(" ");
        return commands[commands.length - 1];
    }

    /**
     * get all startWith 'prefix' command
     *
     * @param preFix prefix
     * @return java.util.List<java.lang.String>
     */
    private List<String> findTargetCommands(String preFix) {
        return commandsSet.stream().filter(command -> command.startsWith(preFix)).collect(Collectors.toList());
    }

    /**
     * Find the longest identical character except prefix
     * And put the matching commands into the collection to be displayed
     *
     * @param preFix prefix to complete
     * @param attributes Line extension info ui format
     * @return The longest common part except for the same prefix
     */
    private String findTargetCommand(String preFix, TextAttributes attributes) {
        clearLineExtensionInfos();
        List<String> fullCommands = findTargetCommands(preFix);
        if (fullCommands.isEmpty()) {
            return "";
        }
        if (fullCommands.size() == 1) {
            return fullCommands.get(0).substring(preFix.length()) + " ";
        }
        LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(fullCommands.toString(), attributes);
        lineExtensionInfos.add(lineExtensionInfo);
        return getLongestCommonExceptPrefix(fullCommands, preFix);
    }

    /**
     * Gets the longest public part except the prefix eg: ['step','stop'] prefix = 's' return 't'
     *
     * @param fullCommands command
     * @param preFix prefix
     * @return java.lang.String
     */
    public static String getLongestCommonExceptPrefix(List<String> fullCommands, String preFix) {
        String curCommendPrefix = fullCommands.get(0);
        for (int i = 1; i < fullCommands.size(); i++) {
            String next = fullCommands.get(i);
            int equalsMinEnd = Math.min(curCommendPrefix.length(), next.length());
            int equalsCurse = preFix.length();
            while (equalsCurse < equalsMinEnd) {
                if (curCommendPrefix.charAt(equalsCurse) == next.charAt(equalsCurse)) {
                    equalsCurse++;
                } else {
                    break;
                }
            }
            curCommendPrefix = curCommendPrefix.substring(0, equalsCurse);
        }
        return curCommendPrefix.substring(preFix.length());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CommandCompletion)) {
            return false;
        }
        CommandCompletion that = (CommandCompletion) obj;
        return Objects.equals(commandsSet, that.commandsSet)
                && Objects.equals(lineExtensionInfos, that.lineExtensionInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandsSet, lineExtensionInfos);
    }

    @Override
    public String toString() {
        return "InstructionCompletionUtils{}";
    }
}
