package com.gordonlu.openai;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.*;

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

import org.json.JSONObject;
import org.json.JSONArray;

import com.gordonlu.openai.helpers.Model;
import com.gordonlu.openai.helpers.Size;

@DesignerComponent(version = 3, description = "An extension that allows you to use AI and connect with OpenAI. Created by Gordon.", iconName = "aiwebres/icon.png",
 category = ComponentCategory.EXTENSION, nonVisible = true)
@UsesLibraries(libraries = "json-20220924.jar")
@SimpleObject(external = true)

public class OpenAI extends AndroidNonvisibleComponent {

    public OpenAI(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Chats with the OpenAI bot, with your query and your API key. Choose a model to talk to with the model parameter." +
     " The temperature parameter decides the type of answer that the bot will give you. Try values closer to 1 for creative answers, and values closer to 0" + 
     " for informative answers. The temperature parameter MUST be a value between 0 and 1 inclusively.") 
    public void Chat(final String prompt, @Options(Model.class) final String model, final String apiKey, final int maxTokens, final double temperature) {
		AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run () {
                try {
                    final String mod = model;
                    URL url = new URL("https://api.openai.com/v1/completions");
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                    httpConn.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                    JSONObject data = new JSONObject();
                    data.put("model", mod);
                    data.put("prompt", prompt.replace("\"", "'"));
                    data.put("temperature", temperature);
                    data.put("max_tokens", maxTokens);
                    writer.write(data.toString());
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
                                if (obj.has("error")) {
                                    String output = obj.getJSONObject("error").getString("message");
                                    Error(output, "Chat");
                                } else if (obj.has("choices")) {
                                    String output = obj.getJSONArray("choices").getJSONObject(0).getString("text");
                                    int usage = obj.getJSONObject("usage").getInt("total_tokens");
                                    if (output.startsWith("\n\n")) {
                                        output = output.substring(2);
                                    }
                                    RespondedToChat(output, usage);
                                } else {
                                    Error("The process is completed but nothing is returned. The output is:\n" + json, "Chat");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Error(e.getMessage(), "Chat");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(e.getMessage(), "Chat");
                }
            }
        });
    }

    @SimpleFunction(description = "Requests OpenAI to generate an image. The prompt is what the image should draw, such as a monkey holding a banana. " + 
    "The size parameter decides how large the output should be (in pixels); there are only three acceptable values, defined in the helper block Size.") 
    public void GenerateImage(final String prompt, final String apiKey, @Options(Size.class) final String size) {
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
                    JSONObject data = new JSONObject();
                    data.put("size", size);
                    data.put("n", 1);
                    data.put("prompt", prompt.replace("\"", "'"));
                    writer.write(data.toString());
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
                                if(obj.has("data")) {
                                    String output = obj.getJSONArray("data").getJSONObject(0).getString("url");
                                    GeneratedImage(output);
                                } else if (obj.has("error")) {
                                    Error(obj.getJSONObject("error").getString("message"), "GenerateImage");
                                } else {
                                    Error("The process is completed but nothing is returned. The output is:\n" + json, "GenerateImage");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Error(e.getMessage(), "GenerateImage");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(e.getMessage(), "GenerateImage");
                }
            }
        });
    }

    @SimpleEvent(description = "This event is fired when OpenAI has responded to your question from the Chat block!")
    public void RespondedToChat(String response, int tokensSpent) {
        EventDispatcher.dispatchEvent(this, "RespondedToChat", response, tokensSpent);
    }

    @SimpleEvent(description = "This event is fired when OpenAI has generated an image of your choice!")
    public void GeneratedImage(String imageUrl) {
        EventDispatcher.dispatchEvent(this, "GotImage", imageUrl);
    }

    @SimpleEvent(description = "This error is fired when an error has occurred. The block parameter tells you which block has fired the event.")
    public void Error(String error, String block) {
        EventDispatcher.dispatchEvent(this, "Error", error, block);
    }

    // new blocks in version 3

    @SimpleFunction(description = "Requests OpenAI to edit this text, according to the instruction that you have given. " +
    " The temperature parameter decides the type of answer that the bot will give you. Try values closer to 1 for creative answers, and values closer to 0" + 
    " for informative answers. The temperature parameter MUST be a value between 0 and 1 inclusively. This block ONLY uses the Davinci model.") 
    public void EditText(final String input, final String instruction, final String apiKey, final double temperature) {
       AsynchUtil.runAsynchronously(new Runnable() {
           @Override
           public void run () {
               try {
                   final String mod = "text-davinci-edit-001";
                   URL url = new URL("https://api.openai.com/v1/edits");
                   HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                   httpConn.setRequestMethod("POST");
                   httpConn.setRequestProperty("Content-Type", "application/json");
                   httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                   httpConn.setDoOutput(true);
                   OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                   JSONObject data = new JSONObject();
                   data.put("model", mod);
                   data.put("input", input.replace("\"", "'"));
                   data.put("instruction", instruction.replace("\"", "'"));
                   data.put("temperature", temperature);
                   writer.write(data.toString());
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
                               if (obj.has("error")) {
                                   String output = obj.getJSONObject("error").getString("message");
                                   Error(output, "Chat");
                               } else if (obj.has("choices")) {
                                   String output = obj.getJSONArray("choices").getJSONObject(0).getString("text");
                                   int usage = obj.getJSONObject("usage").getInt("total_tokens");
                                   if (output.startsWith("\n\n")) {
                                       output = output.substring(2);
                                   }
                                   EditedInput(output, usage);
                               } else {
                                   Error("The process is completed but nothing is returned. The output is:\n" + json, "Chat");
                               }
                           } catch (Exception e) {
                               e.printStackTrace();
                               Error(e.getMessage(), "Chat");
                           }
                       }
                   });
               } catch (Exception e) {
                   e.printStackTrace();
                   Error(e.getMessage(), "Chat");
               }
           }
       });
   }

   @SimpleEvent(description = "This event is fired when OpenAI has edited your input from the EditText block!")
   public void EditedInput(String output, int tokensSpent) {
       EventDispatcher.dispatchEvent(this, "EditedInput", output, tokensSpent);
   }
}
