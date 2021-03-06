package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.File;
import java.util.regex.Pattern;

public class WebDownloadUtils {
    private WebDownloadUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldOpen(String contentDisposition) {
        return (contentDisposition == null || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10));
    }

    public static boolean openFile(Context context, String url, String mimetype) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), mimetype);

        if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty())
            return false;

        context.startActivity(intent);
        return true;
    }

    public static File guessDownloadFile(String folder_path, String url, String contentDisposition, String mimetype) {
        if (url.startsWith("data:")) {
            String[] data = url.split(Pattern.quote(","));
            if (data.length > 1) {
                mimetype = data[0].split(Pattern.quote(";"))[0].substring(5);
            }
        }
        String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
        if (TextUtils.isEmpty(filename)) {
            filename = "index.html";
        }

        FileUtils.ParsedFileName pFname = FileUtils.getParsedFileName(filename);
        int i = 1;
        final File folder = new File(folder_path);
        String[] filelist = folder.list();
        if (filelist != null) {
            StringBuilder strbuilder = new StringBuilder();
            while (FileUtils.checkFileExists(filelist, filename)) {
                strbuilder.append(pFname.Prefix).append("-").append(i);
                if (pFname.Suffix != null) {
                    strbuilder.append(".").append(pFname.Suffix);
                }
                filename = strbuilder.toString();
                ++i;
                strbuilder.delete(0, strbuilder.length());
            }
        }

        return new File(folder, filename);
    }


}
