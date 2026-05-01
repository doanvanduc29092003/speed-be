package org.example.speeded.Controller;

import org.example.speeded.Entity.User;
import org.example.speeded.Repository.UserRepository;
import org.example.speeded.Service.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepo;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    // REGISTER
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    // LOGIN
//    @PostMapping("/login")
//    public String login(@RequestBody User request) {
//        User user = userRepo.findByEmail(request.getEmail())
//                .orElseThrow();
//
//        if (!encoder.matches(request.getPassword(), user.getPassword())) {
//            throw new RuntimeException("Sai mật khẩu");
//        }
//
//        return jwtService.generateToken(user.getId());
//    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        String token = jwtService.generateToken(user.getId());

        return Map.of("token", token); //trả JSON
    }

    // ================= GET CURRENT USER =================
    @GetMapping("/me")
    public User getMe(@RequestHeader("Authorization") String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token");
        }

        String token = header.replace("Bearer ", "");

        Long userId = jwtService.getUserId(token);

        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    // sửa user
    @PutMapping("/me")
    public User updateProfile(
            @RequestHeader("Authorization") String header,
            @RequestBody User updatedUser) {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        User user = userRepo.findById(userId).orElseThrow();

        // chỉ update field cho phép
        user.setUsername(updatedUser.getUsername());
        user.setWeight(updatedUser.getWeight());
        user.setImage(updatedUser.getImage()); // 👈 thêm ảnh

        return userRepo.save(user);
    }

    @PostMapping("/upload-avatar")
    public User uploadAvatar(@RequestHeader("Authorization") String header,
                             @RequestParam("file") MultipartFile file) throws Exception {

        // ===== CHECK TOKEN =====
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token");
        }

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        User user = userRepo.findById(userId).orElseThrow();

        // ===== CHECK FILE =====
        if (file.isEmpty()) {
            throw new RuntimeException("File rỗng");
        }

        // ===== LẤY TÊN FILE =====
        String original = file.getOriginalFilename();

        if (original == null || original.isBlank()) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        // 👉 KHÔNG chặn gì cả, chỉ xử lý nhẹ để tránh lỗi hệ thống
        String safeName = original
                .trim()
                .replaceAll("\\s+", "_"); // chỉ bỏ khoảng trắng cho đỡ lỗi URL

        String fileName = System.currentTimeMillis() + "_" + safeName;

        // ===== LƯU FILE =====
        String uploadDir = "C:/Users/hp/IdeaProjects/speeded/src/main/java/org/example/speeded/upload"; // 👈 QUAN TRỌNG (tránh tomcat temp)

        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        java.io.File savedFile = new java.io.File(dir, fileName);

        System.out.println("SAVE PATH: " + savedFile.getAbsolutePath());

        file.transferTo(savedFile);

        // ===== SAVE DB =====
        user.setImage("/uploads/" + fileName);

        return userRepo.save(user);
    }

    @PutMapping("/change-password")
    public Map<String, String> changePassword(
            @RequestHeader("Authorization") String header,
            @RequestBody Map<String, String> body) {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        User user = userRepo.findById(userId).orElseThrow();

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        // check mật khẩu cũ
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // set mật khẩu mới
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        return Map.of("message", "Đổi mật khẩu thành công");
    }
}
