/*
  * This file is part of HyperCeiler.

  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

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
import io.github.lingqiqi5211.ezhooktool.xposed.java.IReplaceHook;

public class DisableUploadAppListNew extends BaseHook {
    private static final String APP_LIST_UPLOAD_URL = "https://flash.sec.miui.com/detect/app";
    private Method mUploadMethod;

    @Override
    protected boolean useDexKit() {
        return true;
    }

    @Override
    protected boolean initDexKit() {
        List<Method> uploadMethods = optionalMemberList("AppListUploadRequest", bridge -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .addUsingString(APP_LIST_UPLOAD_URL, StringMatchType.Equals)
                .addUsingString("POST", StringMatchType.Equals)
                .modifiers(Modifier.STATIC)
                .paramTypes("com.miui.guardprovider.GuardApplication", "org.json.JSONObject")
                .returnType(String.class)
            )));
        if (uploadMethods.size() == 1) {
            mUploadMethod = uploadMethods.getFirst();
            return true;
        }
        if (!uploadMethods.isEmpty()) {
            XposedLog.w(TAG, getPackageName(), "Multiple app list upload requests found: " + uploadMethods.size());
            return false;
        }

        mUploadMethod = optionalMember("AntiDefraudAppManager", bridge -> bridge.findMethod(FindMethod.create()
            .matcher(MethodMatcher.create()
                .usingStrings("AntiDefraudAppManager", APP_LIST_UPLOAD_URL)
            )).singleOrNull());
        if (mUploadMethod == null) {
            XposedLog.w(TAG, getPackageName(), "No unique app list upload method found");
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        XposedLog.d(TAG, getPackageName(), "Hooking app list upload: " + mUploadMethod);
        hookMethod(mUploadMethod, new IReplaceHook() {
            @Override
            public Object replace(HookParam param) {
                return null;
            }
        });
    }
}
