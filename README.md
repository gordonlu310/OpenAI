**:computer: Introduction**

Do you want to chat with AI? Generate an image from your command? Well, this is the extension for you!

 <img src="https://user-images.githubusercontent.com/88015331/214205457-8459ae11-a024-4c49-b11c-7546f3a688e5.jpg" width=40% height=40%> <img src="https://user-images.githubusercontent.com/88015331/214205413-e5ba8337-5e37-42ff-b173-48c3ea18dfb2.jpeg" width=40% height=40%>


This is an extension that allows you to integrate OpenAI and GPT-3 with AppInventor. You can ask OpenAI questions, chat with it, and even generate images!

**:chart_with_upwards_trend: Stats**

**:hammer_and_wrench: Built with:** Extension Template

**:calendar: Released on:** [date=2023-01-15 timezone="Asia/Hong_Kong"]

**:clock1: Version:** 2

**:books: Documentation**

***Method blocks***

>
>**Chat**
>
>![image](https://user-images.githubusercontent.com/88015331/214204629-bc6021e7-c123-4661-9f70-44cff491f310.png)
>
>Chats with OpenAI. The `prompt` parameter specifies what the query/question is, such as "What color is the sky?" or even better questions.
>* The prompt parameter tells the extension the question that you want to ask.
>
>* The apiKey parameter tells the extension the API key of your OpenAI account.
>
>* The maxTokens parameter limit the length of the answer that you will get. For example, in order to get an answer of around one or two sentences, set this as 250 to be safe. You may get a short or imcomplete answer if you set this too low. All free users can spend 150K tokens every minute.
>
>* Do you want an informative or a creative answer? If the informative parameter is false, the bot will give a creative response. This is useful for questions requiring imagination. Otherwise, if you are asking for facts, set this as true.
>
>*Parameters:* prompt = text, apiKey = text, maxTokens = text, informative = boolean

>
>**GenerateImage**
>
>![image](https://user-images.githubusercontent.com/88015331/214204647-5e557d89-1f33-4f26-935d-cb7d7fc3d385.png)
>
>Generates an image according to the prompt. The prompt tells the extension what the image is about, such as "Monkey holding a banana". The apiKey tells the extension your OpenAI API key.
>
>*Parameters:* prompt = text, apiKey = text

***Event blocks***

>
>**Error**
>
>![image](https://user-images.githubusercontent.com/88015331/214204669-dd25ab4e-a026-43c4-a1d5-a449feeaaa74.png)
>
>This event is fired when an error has occurred.
>
>*Parameters:* error = text

>
>**GotImage**
>
>![image](https://user-images.githubusercontent.com/88015331/214204679-32abb860-cb00-4724-a6fb-14a2bfb43772.png)
>
>This event is fired when the extension has generated an image! You can feed an Image component directly with this URL.
>
>*Parameters:* imageUrl = text

>
>**GotResponse**
>
>![image](https://user-images.githubusercontent.com/88015331/214204699-1832ae5a-606f-4c11-bbc3-cb8a9fe1de13.png)
>
>This event is fired when the extension has responded to your question. The tokensSpent parameter indicates how many tokens you have spent from your OpenAI account in order to ask this question.
>
>*Parameters:* response = text, tokensSpent = number (int)

**:green_book: FAQ**

**Q1:** Why is my answer incomplete? Is this a bug in the extension?

**A1:** No, this is not a bug. Try to increase your maxTokens parameter for this. The maxTokens parameter judges how long the response should be, and with lower tokens, you get shorter, or maybe incomplete, answers.

**Q2:** How do I obtain an API key?

**A2:** @Dayron_Miranda was generous enough to give us a few steps. Not all countries support OpenAI though, for example, we Chinese do not get to use OpenAI.

> 1. Go to the OpenAI website (https://openai.com/).
> 
> 2. Click on the “Developers” button in the top right corner of the page.
> 
> 3. Click on the “API” button in the top right corner of the page.
> 
> 4. Click on the “Get API Key” button in the top right corner of the page.
> 
> 5. Fill in the required information and click on the “Create API Key” button.
> 
> 6. Once you have your API key, you can test GPT chat by using one of the many libraries or sample codes available on the OpenAI website, or by making a call to the API endpoint directly with the API key. In this instance, copy the API key for this extension to work.

**Q3:** Is there a quota for this?

**A3:** Yes, unfortunately. Please see https://openai.com/api/pricing/ for details.

**Q4:** Why do I get a "No value for choices" error?

**A4:** This is caused maybe because you include a special character. Do not use double quotes in your prompts ("), use single quotes ('). Also check if you completed the setup correctly.

**Q5:** Where is the source code?

**A5:** It's in this repository in GitHub.

https://github.com/GordonL0049/OpenAI

**:+1: Credits**

Thanks again to @Kumaraswamy (https://community.appinventor.mit.edu/u/kumaraswamy) for guidance and help in creating the extension, also Shreyash for his tutorials in creating the extension! (it's here: https://community.kodular.io/t/learn-how-to-create-extensions/65492?u=gordon_lu)

Also, special thanks to @Dayron_Miranda (https://community.appinventor.mit.edu/u/dayron_miranda) , who provided an API key for me to test this extension, and also participating in the beta-test program himself. Shoutout to him! Without his help, this extension would not be possible. (OpenAI is not supported in Hong Kong)

Please, if you like this extension or if you want to support me, click the beautiful <kbd> :heart: <b>Like</kbd></b> button of this extension in the AppInventor community post, this took two whole mornings and one whole afternoon to make, and I genuinely appreciate your kindness. Have a nice day!

https://community.appinventor.mit.edu/t/f-os-artificial-intelligence-and-openai/74469?u=gordon_lu
