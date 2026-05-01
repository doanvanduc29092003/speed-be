package org.example.speeded.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.speeded.Entity.ChatMessage;
import org.example.speeded.Entity.ChatSession;
import org.example.speeded.Service.ChatService;
import org.example.speeded.Service.JwtService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/chat")
@CrossOrigin
public class ChatController {

    private final String apiKey = "AIzaSyAGL2dvpctzYxbd3AOfar6-KAA5aYUV1_Y"; // 🔥 đổi key mới
    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatService chatService;
    private final JwtService jwtService;

    private List<String> cachedModels = new ArrayList<>();
    private long lastFetchTime = 0;

    public ChatController(ChatService chatService, JwtService jwtService) {
        this.chatService = chatService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public Map<String, Object> chat(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> req
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserId(token);

        String msg = req.get("message");
        String sessionIdStr = req.get("sessionId");

        Long sessionId;

        // 👉 chưa có session → tạo mới
        if (sessionIdStr == null || sessionIdStr.isEmpty()) {
            sessionId = chatService.createSession(userId, msg);
        } else {
            sessionId = Long.parseLong(sessionIdStr);
        }

        // 👉 lưu user message
        chatService.saveMessage(sessionId, "user", msg);

        // ===== RULE =====
        String reply;
        String lowerMsg = msg.toLowerCase();

        if (lowerMsg.contains("bao nhiêu km")) {
            reply = "Bạn nên chạy 3-5km nếu mới bắt đầu 🏃";
        } else if (lowerMsg.contains("calo")) {
            reply = "Chạy 5km đốt khoảng 300-400 calo 🔥";
        } else if (lowerMsg.contains("không mệt")) {
            reply = "Éo chạy là éo mệt 😆";
        } else {
            reply = callAI(msg);
        }

        // 👉 lưu bot message
        chatService.saveMessage(sessionId, "bot", reply);

        return Map.of(
                "reply", reply,
                "sessionId", sessionId
        );
    }

    // ================= LẤY MODEL =================
    private List<String> getAvailableModels() {

        // cache 60s
        if (System.currentTimeMillis() - lastFetchTime < 60_000 && !cachedModels.isEmpty()) {
            return cachedModels;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            List<String> models = new ArrayList<>();

            for (JsonNode model : json.get("models")) {
                if (model.get("supportedGenerationMethods").toString().contains("generateContent")) {
                    models.add(model.get("name").asText());
                }
            }

            cachedModels = models;
            lastFetchTime = System.currentTimeMillis();

        } catch (Exception e) {
            System.out.println("❌ Lỗi lấy model: " + e.getMessage());
        }

        return cachedModels;
    }

    // ================= AI =================
    private String callAI(String message) {

        List<String> models = getAvailableModels();

        int count = 0;

        for (String model : models) {
            if (count++ > 3) break; // 🔥 giới hạn thử

            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/"
                        + model + ":generateContent?key=" + apiKey;

                Map<String, Object> body = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text",
                                                "Bạn là HLV chạy bộ. Trả lời ngắn gọn, dễ hiểu, có emoji.\nCâu hỏi: " +
                                                "Luôn trả lời bằng tiếng Việt 100%, Không dùng tiếng Anh.\n" +
                                                        "Từ chuyên nghành hay đặc thù thì nói tiếng anh thôi.\n" +message)
                                ))
                        ),
                        "generationConfig", Map.of(
                                "maxOutputTokens", 2000
                        )
                );

                String response = restTemplate.postForObject(url, body, String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response);

                String reply = json.get("candidates").get(0)
                        .get("content").get("parts").get(0).get("text").asText();

                return reply;

            } catch (Exception e) {
                System.out.println("❌ Model lỗi: " + model + " | " + e.getMessage());

                try {
                    Thread.sleep(1000); // 🔥 delay chống 429
                } catch (InterruptedException ignored) {}
            }
        }

        return "🤖 AI đang hơi bận, thử lại sau nhé!";
    }

    @GetMapping("/history/{sessionId}")
    public List<ChatMessage> history(@PathVariable Long sessionId) {
        return chatService.getMessages(sessionId);
    }

    @GetMapping("/sessions")
    public List<ChatSession> sessions(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserId(token);

        return chatService.getSessions(userId);
    }

    @DeleteMapping("/{sessionId}")
    public void delete(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserId(token);

        chatService.deleteSession(sessionId, userId);
    }
}