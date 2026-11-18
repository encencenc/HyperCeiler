/*
 * This file is part of HyperCeiler.
 *
 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023-2026 HyperCeiler Contributions
 */

package com.sevtinge.hyperceiler.libhook.rules.guardprovider;

import com.sevtinge.hyperceiler.common.log.XposedLog;
import com.sevtinge.hyperceiler.libhook.base.BaseHook;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.enums.StringMatchType;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.github.lingqiqi5211.ezhooktool.xposed.common.HookParam;
import io.github.lingqiqi5211.ezhooktool.xposed.java.IMethodHook;

public class DisableRootedCheck extends BaseHook {
    private List<Method> mCheckRootMethods;

    @Override
    protected boolean useDexKit() {
        return true;
    }

    @Override
    protected boolean initDexKit() {
        mCheckRootMethods = optionalMemberList("RootManagerCheck", bridge -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .addUsingString("root manager found: ", StringMatchType.Equals)
                .modifiers(Modifier.STATIC)
                .paramTypes("com.miui.guardprovider.GuardApplication")
                .returnType(boolean.class)
            )));
        if (mCheckRootMethods.size() == 1) {
            return true;
        }
        if (!mCheckRootMethods.isEmpty()) {
            XposedLog.w(TAG, getPackageName(), "Multiple root manager checks found: " + mCheckRootMethods.size());
            return false;
        }

        mCheckRootMethods = optionalMemberList("CheckRoot", bridge -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .usingStrings("/system/bin/")
                .returnType(boolean.class)
            )));
        if (mCheckRootMethods.isEmpty()) {
            XposedLog.w(TAG, getPackageName(), "No root manager or legacy environment check found");
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        for (Method method : mCheckRootMethods) {
            XposedLog.d(TAG, getPackageName(), "Hooking environment check: " + method);
            hookMethod(method, new IMethodHook() {
                @Override
                public void before(HookParam param) {
                    param.setResult(false);
                }
            });
        }
    }
}
