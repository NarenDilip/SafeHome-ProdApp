package com.salzerproduct.safehome.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.salzerproduct.safehome.R;

/**
 * Created by narendran on 6/5/16.
 */
public class BaseActivity extends Activity {

    public static ProgressDialog progress;

//    public static void exportDB(Context context) {
//        try {
//            File sd = Environment.getExternalStorageDirectory();
//            // File data = Environment.getDataDirectory();
//
//            if (sd.canWrite()) {
//                File path = context.getDatabasePath("HaloDataManager.db");
//                String db_path = path.getAbsolutePath();
//
//                String currentDBPath = "/data/" + context.getPackageName() + "/databases/HaloDataManager.db";
//                String backupDBPath = "/HaloDb/HaloDataManager.db";
//                File currentDB = new File(db_path);
//                File backupDB = new File(sd, backupDBPath);
//                Log.v("create table", "===");
//                if (currentDB.exists()) {
//                    FileChannel src = new FileInputStream(currentDB).getChannel();
//                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
//                    dst.transferFrom(src, 0, src.size());
//                    src.close();
//                    dst.close();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static boolean internetIsAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (netInfo != null
                    && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            } else {
                netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null
                        && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

//    public boolean isValidEmail(CharSequence email) {
//        if (!TextUtils.isEmpty(email)) {
//            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
//        }
//        return false;
//    }
//
//    public boolean isValidPhoneNumber(CharSequence phoneNumber) {
//        if (!TextUtils.isEmpty(phoneNumber)) {
//            return Patterns.PHONE.matcher(phoneNumber).matches();
//        }
//        return false;
//    }

    public static void showProgressDialog(Context context) {
        progress = ProgressDialog.show(context, "Audit",
                "Please wait", true);
    }

    public static void hideProgressDialog(Context context) {
        if (progress != null) {
            progress.dismiss();
        }
    }

    public static void showConnectionNotAvailable(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Audit");
        builder.setMessage("Please white_tick your network connection");
        builder.setPositiveButton("OK", null);
        builder.show();
    }


//    public static void showAlertDialog(Context context, String text) {
//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
//        final AlertDialog OptionDialog = alertDialog.create();
//
//        alertDialog.setTitle("Audit");
//        alertDialog.setMessage(text);
//        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                OptionDialog.dismiss();
//            }
//        });
//        alertDialog.show();
//
//    }

    public Activity getActivity() {
        return BaseActivity.this;
    }

    public static void updateSystemInfo(Context activity) {
        SystemInfo obj = new SystemInfo();
        Gson gson = new Gson();
        try {
            obj.BRAND = android.os.Build.BRAND;
            obj.DEVICE = android.os.Build.DEVICE;
            obj.MANUFACTURER = android.os.Build.MANUFACTURER;
            obj.MODEL = android.os.Build.MODEL;
            obj.PRODUCT = android.os.Build.PRODUCT;
            obj.RELEASE = android.os.Build.VERSION.RELEASE;
            TelephonyManager manager = (TelephonyManager) activity
                    .getSystemService(Context.TELEPHONY_SERVICE);
            obj.SIM_OP_NAME = manager.getSimOperatorName();
            obj.OP_NAME = manager.getNetworkOperatorName();
            PackageInfo pInfo = null;
            pInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), PackageManager.GET_META_DATA);
            obj.APP_VERSION = pInfo.versionName;
            AppState.SYSTEM_INFO = gson.toJson(obj);
            Log.d("", "Calculated system array " + AppState.SYSTEM_INFO);

        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
