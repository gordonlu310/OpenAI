package com.gordonlu.openai;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.*;
import java.util.*;

import java.lang.Exception;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONArray;

import com.gordonlu.openai.helpers.Size;

@DesignerComponent(version = 4, description = "An extension that allows you to use AI and connect with OpenAI. Created by Gordon.", iconName = "aiwebres/icon.png",
 category = ComponentCategory.EXTENSION, nonVisible = true)
@UsesLibraries(libraries = "json-20220924.jar")
@SimpleObject(external = true)

public class OpenAI extends AndroidNonvisibleComponent {

    public OpenAI(ComponentContainer container) {
        super(container.$form());
    }

    HashMap<String, String> models = new HashMap<String, String>() {{
        put("A1", "gpt-3.5-turbo");
        put("A2", "gpt-3.5-turbo-0301");
        put("B1", "gpt-4");
        put("B2", "gpt-4-0314");
        put("B3", "gpt-4-32k");
        put("B4", "gpt-4-32k-0314");
    }};

    @SimpleFunction(description = "Chats with the OpenAI bot, with your query and your API key. Choose a model to talk to with the model parameter." +
     " Higher values for the temperature parameter like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic." + 
     " The temperature MUST be between 0 and 2 inclusively.\nStarting from version 4, you must input a model code instead of the model name from the OpenAI documentation." + 
     " A model code corresponds to a model name. Find a list of model codes and their corresponding model names in the documentation.") 
    public void Chat(final String prompt, final String model, final String apiKey, final int maxTokens, final double temperature) {
		AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run () {
                try {
                    if (models.get(model) == null) {
                        Error("No such model found for model code " + model + ". All possible codes are A1, A2, B1, B2, B3 and B4. Check the documentation for a list" + 
                        " of possible model codes that correspond with an OpenAI model.", "Chat");
                    } else {
                        final String mod = model;
                        URL url = new URL("https://api.openai.com/v1/chat/completions");
                        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                        httpConn.setRequestMethod("POST");
                        httpConn.setRequestProperty("Content-Type", "application/json");
                        httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                        httpConn.setDoOutput(true);
                        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
    
                        JSONObject messageDict = new JSONObject();
                        messageDict.put("role", "user");
                        messageDict.put("content", prompt.replace("\"", "'"));
    
                        JSONArray message = new JSONArray();
                        message.put(messageDict);
    
                        JSONObject data = new JSONObject();
                        data.put("model", models.get(mod));
                        data.put("messages", message);
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
                                        String output = obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                                        int usage = obj.getJSONObject("usage").getInt("total_tokens");
                                        if (output.startsWith("\n\n")) {
                                            output = output.substring(2);
                                        } else if (output.startsWith("\n")) {
                                            output = output.substring(1);
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
                    }
                }  catch (Exception e) {
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
                    data.put("prompt", prompt.replace("\"", "'"));
                    data.put("size", size);
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
                                    Error(obj.getJSONObject("error").getString("message"), "GeneratedImage");
                                } else {
                                    Error("The process is completed but nothing is returned. The output is:\n" + json, "GeneratedImage");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Error(e.getMessage(), "GeneratedImage");
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(e.getMessage(), "GeneratedImage");
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
        EventDispatcher.dispatchEvent(this, "GeneratedImage", imageUrl);
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

   // blocks added for version 4

   @SimpleFunction(description = "The moderation API is a service provided by OpenAI that allows you to check whether a text contains inappropriate or offensive information. " + 
   "Put the text that you want to analyze in the input parameter, and supply the block with an API key.")
   public void ModerateText(final String input, final String apiKey) {
    AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run () {
            try {
                URL url = new URL("https://api.openai.com/v1/moderations");
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/json");
                httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
                httpConn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                JSONObject data = new JSONObject();
                data.put("input", input.replace("\"", "'"));
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
                            } else if (obj.has("results")) {
                                boolean flagged = obj.getJSONArray("results").getJSONObject(0).getBoolean("flagged");
                                JSONObject categories = obj.getJSONArray("results").getJSONObject(0).getJSONObject("categories");
                                JSONObject categoryScoresD = obj.getJSONArray("results").getJSONObject(0).getJSONObject("category_scores");
                                YailDictionary categoryFlags = new YailDictionary();
                                Iterator<String> keys = categories.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    Object value = categories.get(key);
                                    categoryFlags.put(key, value);
                                }
                                YailDictionary categoryScores = new YailDictionary();
                                Iterator<String> keys2 = categoryScoresD.keys();
                                while (keys2.hasNext()) {
                                    String key = keys2.next();
                                    Object value = categoryScoresD.get(key);
                                    categoryScores.put(key, value);
                                }
                                ModeratedText(flagged, categoryFlags, categoryScores);
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

   @SimpleEvent(description = "This event is fired when OpenAI has moderated the text you inputted in the ModerateText block! 'flagged' refers to whether the text contains" + 
   " indecent information in general.")
   public void ModeratedText(boolean flagged, YailDictionary categoryFlags, YailDictionary categoryScores) {
       EventDispatcher.dispatchEvent(this, "ModeratedText", flagged, categoryFlags, categoryScores);
   }

   public boolean stringToBoolean(String input) {
    if (input == "true") {
        return true;
    } else {
        return false;
    }
   }
}

// new blocks added in V5

   @SimpleFunction(description = "Get response in chunks, with your query and your API key. Choose a model to talk to with the model parameter." +
     " Higher values for the temperature parameter like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic." + 
     " The temperature MUST be between 0 and 2 inclusively.\nStarting from version 4, you must input a model code instead of the model name from the OpenAI documentation." + 
     " A model code corresponds to a model name. Find a list of model codes and their corresponding model names in the documentation.") 
    public void Stream(final String prompt, final String model, final String apiKey, final int maxTokens, final double temperature) {
		AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run () {
		try {
			if (models.get(model) == null) {
				Error("No such model found for model code " + model + ". All possible codes are A1, A2, B1, B2, B3 and B4. Check the documentation for a list" + 
				" of possible model codes that correspond with an OpenAI model.", "Chat");
			} else {
				final String mod = model;
				URL url = new URL("https://api.openai.com/v1/chat/completions");
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				httpConn.setRequestMethod("POST");
				httpConn.setRequestProperty("Content-Type", "application/json");
				httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
				httpConn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

				JSONObject messageDict = new JSONObject();
				messageDict.put("role", "user");
				messageDict.put("content", prompt.replace("\"", "'"));

				JSONArray message = new JSONArray();
				message.put(messageDict);

				JSONObject data = new JSONObject();
				data.put("model", models.get(mod));
				data.put("messages", message);
				data.put("temperature", temperature);
				data.put("max_tokens", maxTokens);
				data.put("stream", true);

				writer.write(data.toString());
				writer.flush();
				writer.close();
				httpConn.getOutputStream().close();
						
				InputStream responseStream = httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream();

				BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
				StringBuilder response = new StringBuilder();

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
					final String inputLineFinal = inputLine; //final variable to use in the Runnable
					form.runOnUiThread(new Runnable() {
						public void run() {
							GotStream(inputLineFinal);
							}
						});
					}
					in.close();
					httpConn.disconnect();
				}
			}  catch (Exception e) {
				e.printStackTrace();
				Error(e.getMessage(), "Chat");
			}
		}  
	});
}

    @SimpleEvent(description = "This event is fired when OpenAI has responded to your stream request.")
    public void GotStream(String response) {
        EventDispatcher.dispatchEvent(this, "GotStream", response);
    }
