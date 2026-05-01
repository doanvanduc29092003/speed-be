package org.example.speeded.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.speeded.Service.ExportService;
import org.example.speeded.Service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@CrossOrigin
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private JwtService jwtService;

    // ===== EXPORT EXCEL =====
    @GetMapping("/excel")
    public void exportExcel(@RequestHeader("Authorization") String header,
                            HttpServletResponse response) throws Exception {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        exportService.exportExcel(userId, response);
    }

    // ===== EXPORT WORD =====
    @GetMapping("/word")
    public void exportWord(@RequestHeader("Authorization") String header,
                           HttpServletResponse response) throws Exception {

        String token = header.substring(7);
        Long userId = jwtService.getUserId(token);

        exportService.exportWord(userId, response);
    }
}