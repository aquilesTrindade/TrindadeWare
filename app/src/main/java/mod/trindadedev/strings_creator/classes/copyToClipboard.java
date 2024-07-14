package mod.trindadedev.strings_creator.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class copyToClipboard {
    public static void copy(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }
}