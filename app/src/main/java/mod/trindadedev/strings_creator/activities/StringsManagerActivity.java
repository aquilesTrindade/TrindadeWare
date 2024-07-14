package mod.trindadedev.strings_creator.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mod.trindadedev.strings_creator.StringsCreatorApp;
import mod.trindadedev.strings_creator.classes.FileUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import com.sketchware.remod.R;
import mod.trindadedev.strings_creator.adapters.StringsAdapter;
import mod.trindadedev.strings_creator.classes.SimpleHighlighter;
import mod.trindadedev.strings_creator.classes.copyToClipboard;
import mod.trindadedev.theme.ThemedActivity;

public class StringsManagerActivity extends ThemedActivity {

    ExtendedFloatingActionButton fab;
    MaterialToolbar toolbar;
    HashMap<String, Object> map = new HashMap<>();
    String result = "";
    int p = 0;
    Context ctx;
    int wrap = LinearLayout.LayoutParams.WRAP_CONTENT;
    int match = LinearLayout.LayoutParams.MATCH_PARENT;
    private static final int CREATE_FILE = 1;
    ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();
    SharedPreferences sp;
    ListView listStrings;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        sp = getSharedPreferences("sp", Activity.MODE_PRIVATE);
        setContentView(R.layout.layout_alert);
        try{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
	   	} else {
			initializeViews();
            clicks();
	   	}
        }catch(Exception e){
            Toast.makeText(StringsManagerActivity.this, e.toString(), 4000).show();
            Log.e("Error", e.toString());
        }
    }
    
    @Override
	public void onRequestPermissionsResult(int rc, String[] perm, int[] gr) {
		super.onRequestPermissionsResult(rc, perm, gr);
		if (rc == 1000) {
			initializeViews();
             clicks();
		}
	}
    
    
	@Override
	protected void onActivityResult(int rC, int rsC, Intent dt) {
		super.onActivityResult(rC, rsC, dt);
        if (rC == 1 && rsC == RESULT_OK) {
	       if (dt != null) {
			  Uri uri = dt.getData();
			  try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
					outputStream.write(generateCodeFull().getBytes());
				    outputStream.close();
			  } catch (IOException e) {
				    e.printStackTrace();
			  }
		   }
		}
	}
    
    @Override
    public void onPause(){
        super.onPause();
        putData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    private void initializeViews(){
        setContentView(R.layout.strings_manager_activity);
        fab = findViewById(R.id._fab);
        toolbar = findViewById(R.id.toolbar);
        listStrings = findViewById(R.id.listStrings);
       if (!sp.contains("JSON")) {
         sp.edit().putString("JSON", "[]").apply();
       } else {
         getData();
        }
    }
    
    
    public void clicks() {
        try{
            fab.setOnClickListener(v ->{
             MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(StringsManagerActivity.this);
             View alertD = getLayoutInflater().inflate(R.layout.edittext, null);
             dialog.setView(alertD);

            final TextInputEditText stringName =  alertD.findViewById(R.id.stringName);
            final TextInputEditText stringValue = alertD.findViewById(R.id.stringValue);
            final TextInputLayout til1 = alertD.findViewById(R.id.til1);
            final TextInputLayout til2 = alertD.findViewById(R.id.til2);
            stringName.setFocusableInTouchMode(true);
            stringValue.setFocusableInTouchMode(true);
            til1.setBoxCornerRadii((float) 12, (float) 12, (float) 12, (float) 12);
            til2.setBoxCornerRadii((float) 12, (float) 12, (float) 12, (float) 12);
            dialog.setTitle(getResources().getString(R.string.main_fab_text));
            dialog.setPositiveButton(getResources().getString(R.string.create), (d, w) ->{
                              newString(stringName.getText().toString(), stringValue.getText().toString());
            });
            dialog.setNegativeButton(getResources().getString(R.string.cancel), (d, w) ->{           
            });
            dialog.show();       
        });
            
            toolbar.setOnMenuItemClickListener(item ->{
               if (item.getItemId() == R.id.view_code){
                  dialogCode();
               } else if(item.getItemId() == R.id.save_to_file){
                   Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                   intent.addCategory(Intent.CATEGORY_OPENABLE);
                   intent.setType("xml/plain");
                   intent.putExtra(Intent.EXTRA_TITLE, "strings.xml");
                   startActivityForResult(intent, CREATE_FILE);
               }
             return false;
            });
        }catch(Exception s){
            Toast.makeText(ctx, s.toString(), 4000).show();
        }
    }

    public void updateList() {
        StringsCreatorApp.updateListView(StringsManagerActivity.this, listmap, listStrings);
    }
    private void putData(){
        sp.edit().putString("JSON", new Gson().toJson(listmap)).apply();
    }
    private void getData(){
        listmap = new Gson().fromJson(sp.getString("JSON", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
        updateList();
    }
    
    private String forEach(){
        result = "";
        p = listmap.size() - 1;
       for(int _repeat68 = 0; _repeat68 < (int)(listmap.size()); _repeat68++) {
     	result = result.concat("\n   " + listmap.get((int)p).get("val").toString());
    	p--;
       }
        return result;
    }
    
    private String generateCodeFull(){
        String re = "<resources>" + forEach() + "\n</resources>";
        return re;
    }
    
    private void copyText(String text){
       copyToClipboard.copy(StringsManagerActivity.this, text);
        Toast.makeText(StringsManagerActivity.this, "Copiado", 4000).show();
       System.out.println("Copiado " + text);
    }
    
    
    private void newString(String name, String value){
        map = new HashMap();
        map.put("val", "<string name=\"" + name + "\">" + value + "</string>");
        map.put("name", name);
        map.put("value", value);
        listmap.add(map);
        updateList();
    }
    
    private void dialogCode(){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(StringsManagerActivity.this);
        dialog.setTitle(getResources().getString(R.string.view_code));
        //dialog.setMessage(generateCodeFull());
        
        LinearLayout layout = new LinearLayout(StringsManagerActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(match, wrap);
        layout.setPadding(12, 12, 12, 12);
        layout.setLayoutParams(layoutParams);
        
        
        TextView txt = new TextView(StringsManagerActivity.this);
        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(wrap, wrap);
        txtParams.setMargins(18, 20, 20, 20);
        txt.setLayoutParams(txtParams);
        txt.setTextSize((int)10);
        
        layout.addView(txt);
        
        txt.setText(generateCodeFull());
        new SimpleHighlighter(txt, "xml");
        dialog.setView(layout);
        dialog.setPositiveButton(getResources().getString(R.string.copy), (d, w) ->{
            copyText(generateCodeFull());
        });
        dialog.setNegativeButton(getResources().getString(R.string.cancel), (d, w) ->{
            
        });
        dialog.show();
    }
}