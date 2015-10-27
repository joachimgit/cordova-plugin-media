/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.media;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;

import java.util.ArrayList;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;

/**
 * This class called by CordovaActivity to play and record audio.
 * The file can be local or over a network using http.
 *
 * Audio formats supported (tested):
 *  .mp3, .wav
 *
 * Local audio files must reside in one of two places:
 *      android_asset:      file name must start with /android_asset/sound.mp3
 *      sdcard:             file name is just sound.mp3
 */
public class AudioHandler extends CordovaPlugin {

    public static String TAG = "AudioHandler";

    ExtAudioRecorder recorder = null;

    /**
     * Constructor.
     */
    public AudioHandler() {

    }

    /**
     * Executes the request and returns PluginResult.
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackContext       The callback context used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        CordovaResourceApi resourceApi = webView.getResourceApi();
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        if (action.equals("startRecordingAudio")) {
            recorder.start();
            this.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus(\"" + args.getString(0) + "\", 1, 2)");
        }
        else if (action.equals("stopRecordingAudio")) {
            recorder.stop();
            this.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus(\""+ args.getString(0) + "\", 1, 4)");
        }
        else if (action.equals("prepareRecordingAudio")) {
            recorder.prepare();
            this.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus(\""+ args.getString(0) + "\", 1, 1)");
        }
        else if (action.equals("getRecordingLevel")) {
            float a = (float)recorder.getMaxAmplitude();
            callbackContext.sendPluginResult(new PluginResult(status, a));
            return true;
        }
        else if (action.equals("create")) {
            String id = args.getString(0);
            String src = FileHelper.stripFileProtocol(args.getString(1));
            recorder = ExtAudioRecorder.getInstanse(false);
            recorder.setOutputFile(src);
        } else if (action.equals("release")) {
            recorder.release();
        }
        else { // Unrecognized action.
            return false;
        }

        callbackContext.sendPluginResult(new PluginResult(status, result));

        return true;
    }

    /**
     * Stop all audio players and recorders.
     */
    public void onDestroy() {
        if (recorder != null) {
            recorder.release();
        }
    }

    /**
     * Stop all audio players and recorders on navigate.
     */
    @Override
    public void onReset() {
        onDestroy();
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     * @return              Object to stop propagation or null
     */
    public Object onMessage(String id, Object data) {

        // If phone message
        if (id.equals("telephone")) {

            // If phone ringing, then pause playing
            if ("ringing".equals(data) || "offhook".equals(data)) {
                // FIXME : stop recording

            }

            // If phone idle, then resume playing those players we paused
            else if ("idle".equals(data)) {

            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

}
