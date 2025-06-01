package com.campus.im.util;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.client.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.theokanning.openai.service.OpenAiService.*;

@Component
public class AIChat {
    @Value("${app.aichat.deepseek.apiKey}")
    private String apiKey;

    private OpenAiService service;

    @PostConstruct
    public void init() {
        // 设置超时时间
        Duration timeout = Duration.ofSeconds(60);
        
        // 创建默认客户端
        OkHttpClient client = defaultClient(apiKey, timeout)
                .newBuilder()
                .build();
                
        // 创建ObjectMapper
        ObjectMapper mapper = defaultObjectMapper();
        
        // 创建自定义baseUrl的Retrofit实例
        Retrofit retrofit = defaultRetrofit(client, mapper)
                .newBuilder()
                .baseUrl("https://api.deepseek.com/v1/")
                .build();
                
        // 创建OpenAiApi实例
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        
        // 使用api实例创建service
        service = new OpenAiService(api);
    }

    public String getResponse(String name, String intro) {
        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统消息
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你是一个专业的广告文案撰写助手"));
        
        // 添加用户消息
        String prompt = "帮我写一条" + name + "的广告文案,用于发在群聊中打广告。以下是商品描述："+ intro +"。字数200字，语言为中文，可以用emoji增加趣味性，可以用简单html标记空格、换行、加粗等简单格式，但不允许复杂的html格式。不要输出多余的回复，直接生成广告：";
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        // 创建请求
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("deepseek-chat")
                .messages(messages)
                .temperature(0.7)
                .build();

        try {
            // 发送请求并获取响应
            String response = service.createChatCompletion(request)
                    .getChoices().get(0)
                    .getMessage().getContent();
            
            System.out.println("AI Response: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("AI请求失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
