/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2011, IBM Corporation
 */

package com.phonegap.plugins.fileopener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
//import android.util.Log;

import org.apache.cordova.CordovaPlugin;

public class FileOpener extends CordovaPlugin {
    private static final String ASSETS = "file:///android_asset/";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        try {
            if (action.equals("openFile")) {
                openFile(args.getString(0));
                callbackContext.success();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        } catch (RuntimeException e) {  // KLUDGE for Activity Not Found
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
        return false;
    }

    private void openFile(String url) throws IOException {
        AssetManager assetManager = cordova.getActivity().getAssets();

        url.replace("/sdcard/", Environment.getExternalStorageDirectory().getAbsolutePath());
        Uri uri = Uri.parse(url);
        if(url.contains(ASSETS)) {
            String filepath = url.replace(ASSETS, "");

            String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());
            String outdir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+cordova.getActivity().getPackageName()+"/";
            String outfile=outdir+filename;
            File fo=new File(outfile);
            fo.getParentFile().mkdirs();
            if (!fo.exists()) {
                InputStream in = assetManager.open(filepath);
                OutputStream out = new FileOutputStream(fo);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }

            uri = Uri.parse("file://" + outfile);
        }

        Intent intent;
        String mimeType = URLConnection.guessContentTypeFromName(url);
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        this.cordova.getActivity().startActivity(intent); // TODO handle ActivityNotFoundException
    }

}


