package com.gordonlu.chatgpt;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.*;
import java.util.*;

import java.lang.Exception;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.appinventor.components.runtime.util.*;

import org.json.JSONObject;
import org.json.JSONArray;

@DesignerComponent(version = 1, description = "An extension that allows you to use AI and connect with ChatGPT.", iconName = "aiwebres/icon.png", category = ComponentCategory.EXTENSION,
nonVisible = true)
@UsesLibraries(libraries = "json-20220924.jar")
@SimpleObject(external = true)

public class ChatGPT extends AndroidNonvisibleComponent {

    public ChatGPT(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Chats with the OpenAI bot, with your query and your API key. If informative is true, the bot will give you an informative answer;" + 
    " otherwise, it will use more creativity and give you a creative answer.") 
    public void Chat(final String prompt, final String apiKey, final int maxTokens, final boolean informative) {
		AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run () {
                try {
                    final String model = "text-ada-001";
                    URL url = new URL("https://api.openai.com/v1/completions");
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                    httpConn.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                    double temperature;
                    if (informative) {
                        temperature = 0;
                    } else {
                        temperature = 0.9;
                    }
                    writer.write("{\n  \"model\": \"" + model + "\",\n  \"prompt\": \"" + prompt.replace("\"", "'") + "\",\n  \"temperature\": " + temperature + ",\n  \"max_tokens\": " + 
                    maxTokens + "\n}");
                    writer.flush();
                    writer.close();
                    httpConn.getOutputStream().close();
                    InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                            ? httpConn.getInputStream()
                            : httpConn.getErrorStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
                    String inputLine;
                    final StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    form.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String json = response.toString();
                                JSONObject obj = new JSONObject(json);
                                String output = obj.getJSONArray("choices").getJSONObject(0).getString("text");
                                int usage = obj.getJSONObject("usage").getInt("total_tokens");
                                GotResponse(output.replace("\n", ""), usage);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Error(e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(e.getMessage());
                }
            }
        });
    }

    @SimpleFunction(description = "Asks OpenAI to generate an image. The prompt is what the image should draw, such as a monkey,") 
    public void GenerateImage(final String prompt, final String apiKey) {
		AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run () {
                try {
                    URL url = new URL("https://api.openai.com/v1/images/generations");
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                    httpConn.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                    writer.write("{\n  \"size\": \"" + "1024x1024" + "\",\n  \"prompt\": \"" + prompt + "\",\n  \"n\": " + 1 + "\n}");
                    writer.flush();
                    writer.close();
                    httpConn.getOutputStream().close();
                    InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                            ? httpConn.getInputStream()
                            : httpConn.getErrorStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
                    String inputLine;
                    final StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    form.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String json = response.toString();
                                JSONObject obj = new JSONObject(json);
                                String output = obj.getJSONArray("data").getJSONObject(0).getString("url");
                                GotImage(output);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Error(e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(e.getMessage());
                }
            }
        });
    }

    @SimpleEvent(description = "This event is fired when OpenAI has responded to your question!")
    public void GotResponse(String response, int tokensSpent) {
        EventDispatcher.dispatchEvent(this, "GotResponse", response, tokensSpent);
    }

    @SimpleEvent(description = "This event is fired when OpenAI has generated an image of your choice!")
    public void GotImage(String imageUrl) {
        EventDispatcher.dispatchEvent(this, "GotImage", imageUrl);
    }

    @SimpleEvent(description = "This error is fired when an error has occurred.")
    public void Error(String error) {
        EventDispatcher.dispatchEvent(this, "Error", error);
    }
}
