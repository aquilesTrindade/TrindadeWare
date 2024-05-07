package mod.trindade.dev.gslang.classes.interpreter;

//Android
import android.content.Context;
import android.widget.Toast;

import mod.trindade.dev.gslang.methods.MethodCaller;

import com.besome.sketch.MainActivity;

public class BaseCompiller {
	
	String[] parts;
	MethodCaller methodCaller;
	Context mCtx;
	
	
	public BaseCompiller(Context ctx){
		this.mCtx = ctx;
        methodCaller = new MethodCaller(mCtx, new MainActivity());
	}
	
	public void compile(String codeToRun){
		String codeText = codeToRun;
		parts = codeText.split(" ");
		if (methodTyped("createButton")) {
			methodCaller.callMethod(parts[0], parts[1], parts[2]);
			} else if (methodTyped("createText")) {
			methodCaller.callMethod(parts[0], parts[1], parts[2]);
			} else if (methodTyped("showToast")) {
			methodCaller.callMethod(parts[0], parts[1]);
			} else if (methodTyped("openTerminal")) {
			methodCaller.callMethod(parts[0]);
			} else if (methodTyped("clear"))  {
			methodCaller.callMethod(parts[0]);
			} else if (methodTyped("showDialog")) {
			methodCaller.callMethod(parts[0], parts[1], parts[2]);
			} else {
			Toast.makeText(mCtx, "Nenhum método encontrado", 4000).show();
		}
	}
	
	public boolean methodTyped(String methodName){
		boolean returnVal;
		if (parts[0].contains(methodName)) {
			returnVal = true;
			} else {
			returnVal = false;
		}
		return  returnVal;
	}
	
}