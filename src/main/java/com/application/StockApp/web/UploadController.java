package com.application.StockApp.web;

import com.application.StockApp.stock.service.StockService;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final StockService stockService;

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a CSV file to upload.");
            return "upload";
        }

        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            stockService.importCsv(tempFile.getAbsolutePath());
            model.addAttribute("success", "✅ File imported successfully: " + file.getOriginalFilename());
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            model.addAttribute("error", "❌ Failed to import file: " + e.getMessage());
        }

        return "upload";
    }
}
