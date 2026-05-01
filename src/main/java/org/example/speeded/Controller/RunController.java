package org.example.speeded.Controller;

import org.example.speeded.Entity.Run;
import org.example.speeded.Entity.User;
import org.example.speeded.Repository.RunRepository;
import org.example.speeded.Repository.UserRepository;
import org.example.speeded.Service.JwtService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
@CrossOrigin
public class RunController {

    private final RunRepository runRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public RunController(RunRepository runRepo, UserRepository userRepo, JwtService jwtService) {
        this.runRepo = runRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @PostMapping
    public Run saveRun(@RequestHeader("Authorization") String header,
                       @RequestBody Run run) {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        User user = userRepo.findById(userId).orElseThrow();

        run.setUser(user);

        return runRepo.save(run);
    }

    @GetMapping
    public List<Run> getRuns(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        return runRepo.findByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public String deleteRun(@RequestHeader("Authorization") String header,
                            @PathVariable Long id) {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        Run run = runRepo.findById(id).orElseThrow();

        // 🔥 chỉ cho phép xóa run của chính user
        if (!run.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa");
        }

        runRepo.delete(run);

        return "Xóa thành công";
    }
}