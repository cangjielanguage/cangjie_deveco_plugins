/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 * This source file is part of the Cangjie project, licensed under Apache-2.0
 * with Runtime Library Exception.
 *
 * See https://cangjie-lang.cn/pages/LICENSE for license information.
 */

package com.huawei.cangjie.debugger.breakpoint.properties;

import com.huawei.bitfun.utils.CodeCheckByPassUtils;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The type Source x breakpoint properties.
 *
 * @param <T> the type parameter
 * @since 2023 -02-06
 */
public class CangjieBreakpointFiltersProperties<T extends CangjieBreakpointFiltersProperties<?>>
    extends XBreakpointProperties<T> {
    private boolean isHitCountEnabled = false;

    private String hitCount;

    @Nullable
    @Override
    public T getState() {
        return CodeCheckByPassUtils.cast(this);
    }

    @Override
    public void loadState(@NotNull T state) {
        setHitCountEnabled(state.isHitCountEnabled());
        setHitCount(state.getHitCount());
    }

    /**
     * Is pass count enabled boolean.
     *
     * @return the boolean
     */
    @OptionTag("hit-count-filter-enabled")
    public boolean isHitCountEnabled() {
        return isHitCountEnabled;
    }

    /**
     * Sets hit count enabled.
     *
     * @param isHitCountEnabled the hit count enabled
     * @return Indicates whether the hit count enabled is changed
     */
    public boolean setHitCountEnabled(boolean isHitCountEnabled) {
        boolean isChanged = this.isHitCountEnabled != isHitCountEnabled;
        this.isHitCountEnabled = isHitCountEnabled;
        return isChanged;
    }

    /**
     * Gets pass count.
     *
     * @return the pass count
     */
    @OptionTag("hit-count-filter")
    public String getHitCount() {
        return hitCount;
    }

    /**
     * Sets hit count.
     *
     * @param hitCount the pass count
     * @return Indicates whether the hit count is changed
     */
    public boolean setHitCount(String hitCount) {
        boolean isChanged = !Objects.equals(this.hitCount, hitCount);
        this.hitCount = hitCount;
        return isChanged;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CangjieBreakpointFiltersProperties)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CangjieBreakpointFiltersProperties<?> that = (CangjieBreakpointFiltersProperties<?>) obj;
        return isHitCountEnabled == that.isHitCountEnabled && Objects.equals(hitCount, that.hitCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isHitCountEnabled, hitCount);
    }

    @Override
    public String toString() {
        return "CangjieBreakpointFiltersProperties{isHitCountEnabled=" + isHitCountEnabled + ", hitCount=" + hitCount
            + "}";
    }
}
