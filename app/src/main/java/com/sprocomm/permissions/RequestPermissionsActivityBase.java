package com.sprocomm.permissions;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.widget.Toast;

import com.sprocomm.mobilebycle.R;

import java.util.ArrayList;

public abstract class RequestPermissionsActivityBase extends Activity {
    public static final String PREVIOUS_ACTIVITY_INTENT = "previous_intent";
    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    private Intent mPreviousActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviousActivityIntent = (Intent) getIntent().getExtras().get(PREVIOUS_ACTIVITY_INTENT);
        if (savedInstanceState == null) {
            requestPermissions();
        }
    }

    protected static boolean startPermissionActivity(Activity activity,
                                                     String[] requiredPermissions, Class<?> newActivityClass) {
        if (!OsUtils.hasPermissions(activity, requiredPermissions)) {
            final Intent intent = new Intent(activity,  newActivityClass);
            intent.putExtra(PREVIOUS_ACTIVITY_INTENT, activity.getIntent());
            activity.startActivity(intent);
            activity.finish();
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (permissions != null && permissions.length > 0
                &&OsUtils.isAllGranted(permissions, grantResults)) {
            mPreviousActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mPreviousActivityIntent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(this, R.string.missing_required_permission, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestPermissions() {
            Trace.beginSection("requestPermissions");
            try {
                // Construct a list of missing permissions
                final ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
                for (String permission : OsUtils.REQUIRED_PERMISSIONS) {
                    if (checkSelfPermission(permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        unsatisfiedPermissions.add(permission);
                    }
                }
                if (unsatisfiedPermissions.size() == 0) {
                    Log.d("wjb sprocomm", "Request permission activity was called even"
                            + " though all permissions are satisfied.");
                    onRequestPermissionsResult(PERMISSIONS_REQUEST_ALL_PERMISSIONS, new String[]{OsUtils.REQUIRED_PERMISSIONS[0]},
                            new int[]{PackageManager.PERMISSION_GRANTED});
                    return;
                }
                requestPermissions(
                        unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions.size()]),
                        PERMISSIONS_REQUEST_ALL_PERMISSIONS);
            } finally {
                Trace.endSection();
            }
        }
    }
