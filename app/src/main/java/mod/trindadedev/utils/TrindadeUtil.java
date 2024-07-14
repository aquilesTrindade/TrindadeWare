package mod.trindadedev.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.besome.sketch.SketchApplication;

public class TrindadeUtil {
   
   public static void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SketchApplication.getContext().startActivity(intent);
   }   
}