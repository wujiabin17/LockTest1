/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprocomm.permissions;

import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest.permission;
import java.util.ArrayList;
import java.util.Arrays;

public class OsUtils {

    public static final String[] REQUIRED_PERMISSIONS = new String[]{
            permission.CAMERA,
            permission.READ_CONTACTS,
            permission.ACCESS_FINE_LOCATION,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_PHONE_STATE
    };

    public static boolean hasPermissions(Context context, String[] permissions) {
        try {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } finally {
        }
    }

    public static boolean isAllGranted(String permissions[], int[] grantResult) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResult[i] != PackageManager.PERMISSION_GRANTED
                    && isPermissionRequired(permissions[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPermissionRequired(String p) {
        return Arrays.asList(REQUIRED_PERMISSIONS).contains(p);
    }
}
