/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.projectmgmt.sync.upgrade.impl;

import static com.huawei.cangjie.projectmgmt.resources.CangjieProjectMgmtBundle.message;
import static com.huawei.cangjie.projectmgmt.utils.FileUtils.getCangjieTargetCompileOption;
import static com.huawei.cangjie.projectmgmt.utils.FileUtils.getTomlUnitMap;

import com.huawei.cangjie.projectmgmt.extend.toml.CJPMTomlEncoder;
import com.huawei.cangjie.projectmgmt.extend.toml.Toml;
import com.huawei.cangjie.projectmgmt.sync.upgrade.provider.CangjieUpdateProvider;
import com.huawei.cangjie.projectmgmt.sync.upgrade.vo.TomlUpdateTypeData;
import com.huawei.cangjie.projectmgmt.utils.CangjieModulePathType;
import com.huawei.cangjie.projectmgmt.utils.FileUtils;
import com.huawei.cangjie.projectmgmt.utils.NotificationUtil;
import com.huawei.cangjie.projectmgmt.utils.SdkUtils;
import com.huawei.deveco.projectmodel.ohos.model.ModuleModel;
import com.huawei.deveco.projectmodel.ohos.model.ProjectModel;
import com.huawei.deveco.projectmodel.ohos.model.impl.OhosModuleModel;
import com.huawei.deveco.projectmodel.ohos.sync.SyncRequest;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The type Cangjie base toml update.
 *
 * @since 2025 -07-05
 */
public abstract class CangjieBaseTomlUpdateImpl implements CangjieUpdateProvider {
    private static final Logger LOG = Logger.getInstance(CangjieBaseTomlUpdateImpl.class);

    @Override
    public boolean doUpdate(@NotNull ProjectModel projectModel, @NotNull SyncRequest syncRequest) {
        List<ModuleModel> moduleModelList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleModelList)) {
            return false;
        }
        for (ModuleModel moduleModel : moduleModelList) {
            if (!isModuleNeedUpdateCheck(moduleModel)) {
                continue;
            }
            Path tomlPath = getTomlPath(moduleModel);
            Optional<Toml> moduleTomlOpt;
            try {
                moduleTomlOpt = new Toml().read(tomlPath.toFile());
            } catch (IllegalStateException e) {
                continue;
            }
            if (moduleTomlOpt.isEmpty()) {
                continue;
            }
            Toml toml = moduleTomlOpt.get();
            if (!doUpdateToml(tomlPath, toml, moduleModel)) {
                NotificationUtil.notifyInfo(message("upgrade.toml.task.fail"), projectModel.getProject(),
                    NotificationType.ERROR);
                return false;
            }
        }
        return true;
    }

    /**
     * Is need update common boolean.
     *
     * @param projectModel the project model
     * @param sdkDirectory the sdk directory
     * @return the boolean
     */
    protected boolean isNeedUpdateCommon(@NotNull ProjectModel projectModel, String sdkDirectory) {
        String sdkPath = SdkUtils.getSdkPath(projectModel);
        if (sdkPath.isEmpty()) {
            return false;
        }
        Path sdkDirPath = Path.of(sdkPath).resolve(sdkDirectory);
        if (sdkDirPath.toFile().exists()) {
            return false;
        }
        List<ModuleModel> moduleList = projectModel.getModuleModelList();
        if (CollectionUtils.isEmpty(moduleList)) {
            return false;
        }
        for (ModuleModel moduleModel : moduleList) {
            if (isModuleNeedUpdateCheck(moduleModel)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets toml path.
     *
     * @param moduleModel the module model
     * @return the toml path
     */
    @NotNull
    protected Path getTomlPath(ModuleModel moduleModel) {
        return Path.of(FileUtils.getRealCjpmTomlDir(moduleModel, false), "cjpm.toml");
    }

    /**
     * Gets toml path.
     *
     * @param moduleModel the module model
     * @param pathType the path type
     * @return the toml path
     */
    @NotNull
    protected Path getTomlPath(ModuleModel moduleModel, CangjieModulePathType pathType) {
        return Path.of(FileUtils.getRealCjpmTomlDir(moduleModel, pathType), "cjpm.toml");
    }

    /**
     * Do update toml boolean.
     *
     * @param tomlPath the toml path
     * @param toml the toml
     * @param moduleModel the module model
     * @return the boolean
     */
    protected boolean doUpdateToml(Path tomlPath, Toml toml, ModuleModel moduleModel) {
        Map<String, Object> tomlMap = toml.toMap();
        List<TomlUpdateTypeData> tomlUpdateList = getTomlUpdateTypeDataList();
        if (CollectionUtils.isEmpty(tomlUpdateList)) {
            return true;
        }
        for (TomlUpdateTypeData data : tomlUpdateList) {
            List<String> tomlKeys = data.getTomlKey();
            if (tomlKeys == null || tomlKeys.isEmpty()) {
                continue;
            }
            TomlUpdateTypeData.OperationEnum operation = data.getOperation();
            Map<String, Object> lastKeyObjMap = getTomlUnitMap(tomlMap, tomlKeys, operation);
            String lastKey = tomlKeys.get(tomlKeys.size() - 1);
            Object updateValue = data.getUpdateValue();
            if (lastKeyObjMap.isEmpty()) {
                if (operation == TomlUpdateTypeData.OperationEnum.FORCED_REPLACE) {
                    lastKeyObjMap.put(lastKey, updateValue);
                }
            } else {
                if (operation == TomlUpdateTypeData.OperationEnum.FORCED_REPLACE
                    || operation == TomlUpdateTypeData.OperationEnum.REPLACE) {
                    doReplaceToml(data, lastKeyObjMap, lastKey, updateValue);
                } else if (operation == TomlUpdateTypeData.OperationEnum.OVERRIDE) {
                    lastKeyObjMap.put(lastKey, updateValue);
                } else if (operation == TomlUpdateTypeData.OperationEnum.ADD) {
                    doAddTomlValue(lastKeyObjMap, lastKey, updateValue);
                } else {
                    LOG.warn("Unsupported operation type");
                }
            }
        }
        doOtherUpdate(tomlMap, moduleModel);
        try {
            CJPMTomlEncoder encoder = new CJPMTomlEncoder(toml);
            encoder.write(tomlMap, tomlPath.toFile());
        } catch (IOException e) {
            LOG.warn("Error writing TOML file: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Do replace toml.
     *
     * @param data the data
     * @param lastKeyObjMap the last key obj map
     * @param lastKey the last key
     * @param updateValue the update value
     */
    protected void doReplaceToml(TomlUpdateTypeData data, Map<String, Object> lastKeyObjMap, String lastKey,
        Object updateValue) {
        Object objVal = lastKeyObjMap.get(lastKey);
        if (objVal instanceof String oldValue) {
            dealWithOldValue(data, lastKeyObjMap, lastKey, updateValue, oldValue);
        } else if (objVal instanceof List oldList) {
            dealWithOldList(data, lastKeyObjMap, lastKey, updateValue, oldList);
        } else {
            LOG.warn("Unsupported class type");
        }
    }

    /**
     * Do add toml.
     *
     * @param lastKeyObjMap the last key obj map
     * @param lastKey the last key
     * @param updateValue the update value
     */
    protected void doAddTomlValue(Map<String, Object> lastKeyObjMap, String lastKey, Object updateValue) {
        Object objVal = lastKeyObjMap.get(lastKey);
        if (objVal instanceof String oldValue) {
            updateStringValue(lastKeyObjMap, lastKey, oldValue, updateValue);
        } else if (objVal instanceof List oldList) {
            updateListValue(lastKeyObjMap, lastKey, oldList, updateValue);
        } else {
            lastKeyObjMap.put(lastKey, updateValue);
        }
    }

    /**
     * Deal with old value.
     *
     * @param data the data
     * @param lastKeyObjMap the last key obj map
     * @param lastKey the last key
     * @param updateValue the update value
     * @param oldValue the old value
     */
    protected void dealWithOldValue(TomlUpdateTypeData data, Map<String, Object> lastKeyObjMap, String lastKey,
        Object updateValue, String oldValue) {
        Object removeValue = data.getRemoveValue();
        String processedValue = handleRemoveOldValue(oldValue, removeValue);
        if (updateValue instanceof String updateStr) {
            if (!matches(processedValue, updateStr)) {
                processedValue = processedValue + " " + updateStr;
            }
        }
        processedValue = processedValue.replaceAll("\\s{2,}", " ").trim();
        lastKeyObjMap.put(lastKey, processedValue);
    }

    /**
     * Deal with old list.
     *
     * @param data the data
     * @param lastKeyObjMap the last key obj map
     * @param lastKey the last key
     * @param updateValue the update value
     * @param oldList the old list
     */
    protected void dealWithOldList(TomlUpdateTypeData data, Map<String, Object> lastKeyObjMap, String lastKey,
        Object updateValue, List oldList) {
        boolean listModified = false;
        List<Object> removeTargets = getRemoveTargets(data.getRemoveValue());
        if (!removeTargets.isEmpty()) {
            if (performListRemoval(oldList, removeTargets)) {
                listModified = true;
            }
        }
        if (performListAddition(oldList, updateValue)) {
            listModified = true;
        }
        if (listModified) {
            lastKeyObjMap.put(lastKey, oldList);
        }
    }

    /**
     * Gets compile option.
     *
     * @param moduleModel the module model
     * @return the compile option
     */
    protected Optional<String> getCompileOption(ModuleModel moduleModel) {
        if (!FileUtils.isCangjieModule(moduleModel)) {
            return Optional.empty();
        }
        Path tomlPath = getTomlPath(moduleModel);
        if (!tomlPath.toFile().exists() || !(moduleModel instanceof OhosModuleModel)) {
            return Optional.empty();
        }
        String compileOption = getCangjieTargetCompileOption((OhosModuleModel) moduleModel, false);
        if (StringUtils.isEmpty(compileOption)) {
            return Optional.empty();
        }
        return Optional.of(compileOption);
    }

    /**
     * Build generalized regex string.
     *
     * @param valueToMatch the value to match
     * @return the string
     */
    protected String buildGeneralizedRegex(String valueToMatch) {
        String regex = valueToMatch.replace("\\", "/");
        regex = regex.replace("[", "\\[");
        regex = regex.replace("]", "\\]");
        regex = regex.replace("/", "[\\\\/]");
        regex = regex.replace("$", "\\$");
        regex = regex.replace("\"", "\\\"");
        regex = regex.replace("{", "\\{");
        regex = regex.replace("}", "\\}");
        regex = regex.replace(".", "\\.");
        regex = regex.replace("+", "\\+");
        regex = regex.replace("*", "\\*");
        regex = regex.replace("?", "\\?");
        regex = regex.replace("^", "\\^");
        regex = regex.replace("(", "\\(");
        regex = regex.replace(")", "\\)");
        regex = regex.replace("|", "\\|");
        regex = regex.replace(" ", "\\s*");
        return regex;
    }

    /**
     * Is matched boolean.
     *
     * @param sourceStr the source str
     * @param valueToMatch the value to match
     * @return the boolean
     */
    protected boolean isMatched(String sourceStr, String valueToMatch) {
        if (StringUtils.isEmpty(sourceStr) || StringUtils.isEmpty(valueToMatch)) {
            return false;
        }
        String targetStrPattern = buildGeneralizedRegex(valueToMatch);
        try {
            Pattern pattern = Pattern.compile(targetStrPattern);
            Matcher matcher = pattern.matcher(sourceStr);
            if (matcher.find()) {
                return true;
            }
        } catch (PatternSyntaxException e) {
            Pattern pattern = Pattern.compile(Pattern.quote(valueToMatch));
            Matcher matcher = pattern.matcher(sourceStr);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Do other update.
     *
     * @param tomlMap the toml map
     * @param moduleModel the module model
     */
    protected void doOtherUpdate(Map<String, Object> tomlMap, ModuleModel moduleModel) {
        // do nothing
    }

    /**
     * Is module need update check boolean.
     *
     * @param moduleModel the module model
     * @return the boolean
     */
    protected abstract boolean isModuleNeedUpdateCheck(ModuleModel moduleModel);

    /**
     * Gets toml update type data list.
     *
     * @return the toml update type data list
     */
    protected abstract List<TomlUpdateTypeData> getTomlUpdateTypeDataList();

    private String normalizeString(String input) {
        if (input == null) {
            return input;
        }
        return input.trim().replace("\\", "/");
    }

    private boolean matches(String sourceString, Object targetValue) {
        if (!(targetValue instanceof String targetString)) {
            return sourceString.equals(targetValue.toString());
        }
        String normalizedSource = normalizeString(sourceString);
        String normalizedTarget = normalizeString(targetString);
        if (normalizedSource.equals(normalizedTarget)) {
            return true;
        }
        try {
            if (targetString.startsWith("^") || targetString.endsWith("$") || targetString.contains("|")
                || targetString.contains("*") || targetString.contains("?") || targetString.contains("+")) {
                return Pattern.compile(targetString).matcher(sourceString).find();
            } else {
                Pattern pattern = Pattern.compile(Pattern.quote(normalizedTarget));
                return pattern.matcher(normalizedSource).find();
            }
        } catch (PatternSyntaxException e) {
            Pattern pattern = Pattern.compile(Pattern.quote(normalizedTarget));
            return pattern.matcher(normalizedSource).find();
        }
    }

    private void updateStringValue(Map<String, Object> lastKeyObjMap, String lastKey, String oldValue,
        Object updateValue) {
        if (!(updateValue instanceof String updateStr)) {
            return;
        }
        String normalizedUpdateValue = normalizeString(updateStr);
        if (!matches(oldValue, normalizedUpdateValue)) {
            String space = StringUtils.isBlank(oldValue) ? StringUtils.EMPTY : " ";
            String newValue = oldValue + space + updateValue;
            lastKeyObjMap.put(lastKey, newValue);
        }
    }

    private boolean isAlreadyPresent(Set<String> normalizedOldItems, Object valueToCheck) {
        if (valueToCheck instanceof String valueStr) {
            String normalizedValueStr = normalizeString(valueStr);
            for (String normalizedOldItem : normalizedOldItems) {
                if (matches(normalizedOldItem, normalizedValueStr)) {
                    return true;
                }
            }
            return false;
        } else {
            return normalizedOldItems.contains(valueToCheck.toString());
        }
    }

    private boolean addListItemIfAbsent(List oldList, Set<String> normalizedOldItems, Object itemToAdd) {
        if (!isAlreadyPresent(normalizedOldItems, itemToAdd)) {
            oldList.add(itemToAdd);
            return true;
        }
        return false;
    }

    private void updateListValue(Map<String, Object> lastKeyObjMap, String lastKey, List oldList, Object updateValue) {
        Set<String> normalizedOldItems = new HashSet<>();
        for (Object item : oldList) {
            if (item instanceof String) {
                normalizedOldItems.add(normalizeString((String) item));
            } else {
                normalizedOldItems.add(item.toString());
            }
        }
        boolean listModified = false;
        if (updateValue instanceof String updateStr) {
            if (addListItemIfAbsent(oldList, normalizedOldItems, updateStr)) {
                listModified = true;
            }
        } else if (updateValue instanceof List<?> updateList) {
            for (Object item : updateList) {
                if (addListItemIfAbsent(oldList, normalizedOldItems, item)) {
                    listModified = true;
                }
            }
        } else {
            LOG.warn("Unsupported class type for updateValue: " + updateValue.getClass().getName());
        }
        if (listModified) {
            lastKeyObjMap.put(lastKey, oldList);
        }
    }

    private String removeGeneralizedValueFromString(String sourceString, Object valueToRemove) {
        if (sourceString == null || valueToRemove == null) {
            return sourceString;
        }
        String targetPatternString;
        if (valueToRemove instanceof String) {
            targetPatternString = buildGeneralizedRegex((String) valueToRemove);
        } else {
            targetPatternString = buildGeneralizedRegex(valueToRemove.toString());
        }
        try {
            Pattern pattern = Pattern.compile(targetPatternString);
            Matcher matcher = pattern.matcher(sourceString);
            if (matcher.find()) {
                return matcher.replaceAll(StringUtils.EMPTY);
            }
        } catch (PatternSyntaxException e) {
            Pattern pattern = Pattern.compile(Pattern.quote(valueToRemove.toString()));
            Matcher matcher = pattern.matcher(sourceString);
            if (matcher.find()) {
                return matcher.replaceAll(StringUtils.EMPTY);
            }
        }
        return sourceString;
    }

    private String handleRemoveOldValue(String realOldValueParam, Object removeValue) {
        if (removeValue == null) {
            return realOldValueParam;
        }
        String realOldValue = realOldValueParam;
        if (removeValue instanceof String) {
            realOldValue = removeGeneralizedValueFromString(realOldValue, removeValue);
        } else if (removeValue instanceof List<?> removeList) {
            for (Object item : removeList) {
                realOldValue = removeGeneralizedValueFromString(realOldValue, item);
            }
        } else {
            LOG.warn("Unsupported type for removeValue: " + removeValue.getClass().getName());
        }
        return realOldValue;
    }

    private boolean removeItemIfGeneralizedMatch(Object currentItem, Object targetToRemove, Iterator<Object> iterator) {
        if (!(currentItem instanceof String currentItemStr)) {
            return false;
        }
        if (!(targetToRemove instanceof String targetStr)) {
            if (currentItem.equals(targetToRemove)) {
                iterator.remove();
                return true;
            }
            return false;
        }
        String regexPattern = buildGeneralizedRegex(targetStr);
        try {
            if (Pattern.compile(regexPattern).matcher(currentItemStr).matches()) {
                iterator.remove();
                return true;
            }
        } catch (PatternSyntaxException e) {
            if (matches(currentItemStr, targetStr)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private boolean performListRemoval(List<Object> oldList, List<Object> targetsToRemove) {
        boolean removedAny = false;
        Iterator<Object> iterator = oldList.iterator();
        while (iterator.hasNext()) {
            Object currentItem = iterator.next();
            for (Object target : targetsToRemove) {
                if (removeItemIfGeneralizedMatch(currentItem, target, iterator)) {
                    removedAny = true;
                    break;
                }
            }
        }
        return removedAny;
    }

    private List<Object> getRemoveTargets(Object removeValue) {
        List<Object> targets = new ArrayList<>();
        if (removeValue instanceof String) {
            targets.add(removeValue);
        } else if (removeValue instanceof List<?> removeList) {
            targets.addAll(removeList);
        } else if (removeValue == null) {
            return targets;
        } else {
            LOG.warn("Unsupported type for removeValue");
        }
        return targets;
    }

    private void populateNormalizedItems(List<Object> sourceList, Set<String> normalizedItems) {
        for (Object item : sourceList) {
            if (item instanceof String) {
                normalizedItems.add(normalizeString((String) item));
            } else {
                normalizedItems.add(item.toString());
            }
        }
    }

    private boolean performListAddition(List<Object> oldList, Object updateValue) {
        boolean addedAny = false;
        Set<String> currentNormalizedItems = new HashSet<>();
        populateNormalizedItems(oldList, currentNormalizedItems);
        if (updateValue instanceof String updateStr) {
            if (addListItemIfAbsent(oldList, currentNormalizedItems, updateStr)) {
                addedAny = true;
            }
        } else if (updateValue instanceof List<?> updateList) {
            for (Object item : updateList) {
                if (addListItemIfAbsent(oldList, currentNormalizedItems, item)) {
                    addedAny = true;
                }
            }
        } else {
            LOG.warn("Unsupported class type for updateValue in dealWithOldList: " + updateValue.getClass().getName());
        }
        return addedAny;
    }
}
