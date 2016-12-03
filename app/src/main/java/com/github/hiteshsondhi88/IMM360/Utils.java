/*
 * Copyright 2016 LG Electronics Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hiteshsondhi88.IMM360;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.osclibrary.OSCParameterNameMapper;

import org.json.JSONException;
import org.json.JSONObject;
public class Utils {

    public static void showDialog(Context context, String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(body);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_bg));
        dialog.show();
    }


    /**
     * pretty-print the JSON format data
     * @param data data(Object type)
     * @return data(String type)
     */
    public static String parseString(Object data) {
        if(data!= null) {
            try {
                JSONObject json = new JSONObject((String) data);
                return json.toString(4);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Return api state in response data
     * @param response response data
     * @return command state (done or inProgress)
     */
    public static String getCommandState(Object response) {
        if(response != null) {
            try {
                JSONObject jObject = new JSONObject((String) response);

                if (jObject.has(OSCParameterNameMapper.COMMAND_STATE)) {
                    return jObject.getString(OSCParameterNameMapper.COMMAND_STATE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return command Id in response data
     * @param response response data
     * @return command Id
     */
    public static String getCommandId(Object response) {
        if(response!= null) {
            try {
                JSONObject jObject = new JSONObject((String) response);
                return jObject.getString(OSCParameterNameMapper.COMMAND_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return command name in response data
     * @param response response data
     * @return command name
     */
    public static String getCommandName(Object response){
        if(response != null) {
            try {
                JSONObject jObject = new JSONObject((String) response);
                return jObject.getString(OSCParameterNameMapper.NAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object parseInt(String data){
        try{
            return Integer.parseInt(data);
        }catch (NumberFormatException e){
            return data;
        }
    }

    public static Object parseDouble(String data){
        try{
            return Double.parseDouble(data);
        } catch (NumberFormatException e){
            return data;
        }
    }

    public static Object parseBoolean(String data){
        try{
            return Boolean.parseBoolean(data);
        } catch (NumberFormatException e){
            return data;
        }
    }
}

