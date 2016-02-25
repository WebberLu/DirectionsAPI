package com.kymco.directionsapi;

import android.app.ProgressDialog;
import android.content.Context;

public class Utils {
	public static ProgressDialog presentLoadingDialog(Context context) {
        return ProgressDialog.show(context, "", context.getString(R.string.loading));
    }
}
