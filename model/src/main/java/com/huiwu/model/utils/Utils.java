package com.huiwu.model.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefRecord;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class Utils {
    public Utils() {
    }

    public static String createFileName() {
        return DateFormat.format("\'IMG\'_yyyyMMdd_kkmmss", System.currentTimeMillis()) + ".jpg";
    }

    public static NdefRecord[] createNdefRecord(String type, String data) {
        NdefRecord[] bytes1;
        byte[] str1;
        if ("contact".equals(type)) {
            str1 = data.getBytes();
            System.arraycopy(str1, 0, new byte[1 + str1.length], 1, str1.length);
            bytes1 = new NdefRecord[]{new NdefRecord((short) 2, "text/x-vCard".getBytes(Charset.forName("US-ASCII")), new byte[0], str1)};
            return bytes1;
        } else if ("text".equals(type)) {
            str1 = data.getBytes();
            bytes1 = new NdefRecord[]{new NdefRecord((short) 1, NdefRecord.RTD_TEXT, new byte[0], str1)};
            return bytes1;
        } else {
            String str = "";
            if ("call".equals(type)) {
                str = "tel:";
            } else if ("sms".equals(type)) {
                str = "sms:";
            } else if ("email".equals(type)) {
                str = "mailto:";
            }

            byte[] bytes = (str + data).getBytes();
            byte[] new_bytes = new byte[1 + bytes.length];
            System.arraycopy(bytes, 0, new_bytes, 1, bytes.length);
            NdefRecord[] ndefRecords = new NdefRecord[]{new NdefRecord((short) 1, NdefRecord.RTD_URI, new byte[0], new_bytes)};
            return ndefRecords;
        }
    }

    public static String formatDateTime(String datetime) {
        try {
            return DateFormat.format("yyyy-MM-dd kk:mm:ss", Long.parseLong(datetime.substring(6, datetime.length() - 2))).toString();
        } catch (Exception var2) {
            return DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis()).toString();
        }
    }

    public static String formatDateTimeOffLine(long datetime) {
        try {
            return DateFormat.format("yyyy-MM-dd kk:mm:ss", datetime).toString();
        } catch (Exception var3) {
            return DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis()).toString();
        }
    }

    public static String toHexString(byte[] byteArray, int size) {
        if (byteArray != null && byteArray.length >= 1) {
            StringBuilder hexString = new StringBuilder(2 * size);

            for (int i = 0; i < size; ++i) {
                if ((byteArray[i] & 255) < 16) {
                    hexString.append("0");
                }

                hexString.append(Integer.toHexString(255 & byteArray[i]));
                if (i != byteArray.length - 1) {
                    hexString.append("");
                }
            }

            return hexString.toString().toUpperCase();
        } else {
            throw new IllegalArgumentException("this byteArray must not be null or empty");
        }
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);

        for (int i = bArray.length - 1; i >= 0; i--) {
            String sTemp = Integer.toHexString(255 & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }

            sb.append(sTemp.toUpperCase());
        }

        return sb.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }

        return false;
    }

    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }

        return false;
    }

    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }

        return false;
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
    }

    public static void showShortToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showShortToast(int message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showLongToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showLongToast(int message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static String bytes2HexString(byte[] bytes, int start, int end) {
        StringBuffer sb = new StringBuffer();

        for (int i = start; i < start + end; ++i) {
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(bytes[i])}));
        }

        return sb.toString();
    }

    public static byte[] string2Bytes(String s, int length) {
        byte[] bytes1 = s.getBytes();
        byte[] bytes2 = new byte[length];
        System.arraycopy(bytes1, 0, bytes2, 0, Math.min(length, bytes1.length));
        return bytes2;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static Bitmap dealWithPicture(String path, int dealWidth, int dealHeight) {
        FileOutputStream b = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap e = BitmapFactory.decodeFile(path, options);
            int width = e.getWidth();
            int height = e.getHeight();
            if (width > height) {
                Matrix m = new Matrix();
                m.setRotate(90.0F);
                e = Bitmap.createScaledBitmap(e, dealWidth, (int) ((float) height * ((float) dealWidth / (float) width)), true);
                e = Bitmap.createBitmap(e, 0, 0, e.getWidth(), e.getHeight(), m, true);
            } else {
                e = Bitmap.createScaledBitmap(e, dealHeight, (int) ((float) height * ((float) dealHeight / (float) width)), true);
            }

            b = new FileOutputStream(path);
            e.compress(Bitmap.CompressFormat.JPEG, 100, b);
            return e;
        } catch (FileNotFoundException var16) {
            var16.printStackTrace();
            return null;
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException var15) {
                    var15.printStackTrace();
                }
            }

        }

    }

    /**
     * View视图转为BitMap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public static void fileSave(String filename, String content, Context context) {
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public static String fileRead(String filename, Context context) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        String result;
        try {
            fis = context.openFileInput(filename);
            byte[] e = new byte[fis.available()];
            bos = new ByteArrayOutputStream();

            while (fis.read(e) != -1) {
                if (e.length == 0) {
                    return null;
                }

                bos.write(e);
                bos.flush();
            }

            result = new String(bos.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return result;
    }

    public static boolean saveRecordToFile(String file_dir, String message) {
        String dateFormat = "yyyy-MM-dd";
        String monthFormat = "yyyy-MM";
        String timeFormat = "yyyy-MM-dd kk:mm:ss";
        String file_name = DateFormat.format(dateFormat, System.currentTimeMillis()) + ".txt";

        try {
            File folder = new File(Environment.getExternalStorageDirectory(), file_dir);
            if (!folder.exists()) {
                folder.mkdir();
            }

            File files = new File(folder, DateFormat.format(monthFormat, System.currentTimeMillis()).toString());
            if (!files.exists()) {
                files.mkdir();
            }

            File file = new File(files, file_name);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fos = new FileWriter(file, true);
            fos.append(String.format("%n%s%n", new Object[]{DateFormat.format(timeFormat, System.currentTimeMillis())}));
            fos.append(String.format("%n%s%n", new Object[]{message}));
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void saveUserImage(Context context, Bitmap bitmap, String filename) {
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public static Bitmap getUserImage(Context context, String filename) {
        FileInputStream fis = null;

        try {
            fis = context.openFileInput(filename);
            Bitmap var5 = BitmapFactory.decodeStream(fis);
            return var5;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return null;
    }

}

