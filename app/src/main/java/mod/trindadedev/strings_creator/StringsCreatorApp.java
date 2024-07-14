package mod.trindadedev.strings_creator;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;
import mod.trindadedev.strings_creator.adapters.StringsAdapter;

public class StringsCreatorApp  {
    
    
    public static void updateListView(Context ctx, ArrayList<HashMap<String, Object>> listmap, ListView listStrings) {
    	if (ctx != null) {
            listStrings.setAdapter(new StringsAdapter(ctx, listmap, listStrings));
            ((BaseAdapter) listStrings.getAdapter()).notifyDataSetChanged();
            listStrings.setLayoutParams(new LinearLayout.LayoutParams((int) (android.widget.LinearLayout.LayoutParams.MATCH_PARENT), (int) (listmap.size() * 100)));
        } else {
            listStrings.setAdapter(new StringsAdapter(ctx, listmap, listStrings));
            ((BaseAdapter) listStrings.getAdapter()).notifyDataSetChanged();
            listStrings.setLayoutParams(new LinearLayout.LayoutParams((int) (android.widget.LinearLayout.LayoutParams.MATCH_PARENT), (int) (listmap.size() * 100)));
        }
    }
    public static String getOrientation(Context ctx) {
    	Configuration configuration = ctx.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "portrait";
        } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "landscap";
        } else {
            return "undefined";
        }
    }
}