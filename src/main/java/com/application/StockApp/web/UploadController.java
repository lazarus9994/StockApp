package com.application.StockApp.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class UploadController {

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("message", "No file selected.");
            return "upload";
        }

        // TODO: Implement CSV import logic here
        model.addAttribute("message", "File uploaded: " + file.getOriginalFilename());
        return "upload";
    }
}
