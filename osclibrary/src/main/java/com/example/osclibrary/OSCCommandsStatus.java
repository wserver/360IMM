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
package com.example.osclibrary;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Status
 * API: /osc/commands/status
 */
public class OSCCommandsStatus extends HttpAsyncTask {
    private final static String TAG = "OSCCommandsStatus";

    private final String commandId;

    /**
     * Constructor
     * @param id commandId to check status
     */
    public OSCCommandsStatus(String id) {
        //Set url and http method as POST
        super(HTTP_SERVER_INFO.IP + ":" + HTTP_SERVER_INFO.PORT + "/osc/commands/status",
                HttpHeaderPropertyNameMapper.POST);
        commandId = id;
    }

    @Override
    protected Object doInBackground(Void... voids) {
        //Set body for /osc/commands/status API
        JSONObject data = new JSONObject();
        try {
            data.put(OSCParameterNameMapper.COMMAND_ID,commandId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setHttpRequestData(data.toString());
        return super.doInBackground(voids);
    }
}
