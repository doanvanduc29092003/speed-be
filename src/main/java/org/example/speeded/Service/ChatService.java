package org.example.speeded.Service;

import jakarta.transaction.Transactional;
import org.example.speeded.Entity.ChatMessage;
import org.example.speeded.Entity.ChatSession;
import org.example.speeded.Repository.ChatMessageRepository;
import org.example.speeded.Repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatSessionRepository sessionRepo;

    @Autowired
    private ChatMessageRepository messageRepo;

    // 👉 tạo session
    public Long createSession(Long userId, String firstMessage) {
        ChatSession s = new ChatSession();
        s.setUserId(userId);

        // 🔥 auto title
        s.setTitle(firstMessage.length() > 30
                ? firstMessage.substring(0, 30)
                : firstMessage);

        return sessionRepo.save(s).getId();
    }

    // 👉 lưu tin nhắn
    public void saveMessage(Long sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        messageRepo.save(m);
    }

    // 👉 lấy lịch sử
    public List<ChatMessage> getMessages(Long sessionId) {
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // 👉 list session
    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 👉 xoá
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy session"));

        // 🔥 check đúng user
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xoá");
        }

        messageRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
    }
}
