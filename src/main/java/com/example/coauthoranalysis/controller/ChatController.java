package com.example.coauthoranalysis.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final String apiKey = System.getenv("BAILIAN_API_KEY") != null ? System.getenv("BAILIAN_API_KEY") : "sk-your-bailian-api-key";
    private final String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        if (!requestBody.containsKey("model")) {
            requestBody.put("model", "qwen-plus");
        }

        // 开启百炼大模型的联网搜索功能
        Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new java.util.HashMap<>());
        parameters.put("result_format", "message");
        
        // 只有包含“名片”请求时，才强制指定输出格式，并在prompt中引导
        List<Map<String, Object>> messages = (List<Map<String, Object>>) requestBody.get("messages");
        boolean isCardRequest = false;
        if (messages != null && !messages.isEmpty()) {
            Map<String, Object> lastMessage = messages.get(messages.size() - 1);
            String content = (String) lastMessage.get("content");
            if (content != null && (content.contains("生成名片") || content.contains("名片"))) {
                isCardRequest = true;
                lastMessage.put("content", content + "\n\n请联网搜索该学者的真实信息，并严格按照以下 JSON 格式返回名片数据，不要输出任何额外的解释或Markdown标记（不要加```json）：\n{\"name\": \"学者姓名\", \"title\": \"真实职称\", \"org\": \"真实机构\", \"paperCount\": 论文数(整数), \"score\": 影响力评分(0-100), \"research\": \"研究方向\", \"collaborators\": \"合作者名称(逗号分隔)\"}");
            }
        }
        
        // 开启夸克联网搜索
        Map<String, Object> tools = new java.util.HashMap<>();
        tools.put("enable_search", true);
        parameters.put("tools", tools);
        
        requestBody.put("parameters", parameters);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for demonstration when API key is invalid
            String fallbackResponse = "{\"choices\": [{\"message\": {\"content\": \"这是AI学术助手的模拟回复。由于未配置有效的阿里云百炼API Key，系统返回此默认消息。我是一个专注于学者合著网络的AI助手，可以帮您分析学术网络、生成学者名片等。\"}}]}";
            return ResponseEntity.ok(fallbackResponse);
        }
    }
}
