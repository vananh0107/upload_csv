package com.example.demo.controllers;

import com.example.demo.services.StudentService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("formAction", "/student/import");
        return "upload";
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Vui lòng chọn tệp để tải lên");
            return "upload";
        }

        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir + File.separator + file.getOriginalFilename());
            file.transferTo(tempFile);
            List<?> entities = studentService.importStudents(tempFile.getAbsolutePath());
            model.addAttribute("message", "Import thành công,Số lượng bản ghi: " + entities.size());
        } catch (IOException e) {
            model.addAttribute("message", "Lỗi đọc hoặc ghi file: " + e.getMessage());
        } catch (ValidationException e) {
            model.addAttribute("message", "Lỗi kiểm tra dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "upload";
    }

    @GetMapping("/export")
    public String showExportForm(Model model) {
        model.addAttribute("formAction", "/student/export"); // Đường dẫn cho nút Export
        model.addAttribute("downloadAction", "/student/download"); // Đường dẫn cho nút Download
        return "export";
    }

    @PostMapping("/export")
    public String exportStudents(Model model) {
        String filePath = "students.csv";
        try {
            studentService.exportStudents(filePath); // Gọi service để xuất dữ liệu
            model.addAttribute("message", "Dữ liệu đã được xuất thành công");
        } catch (Exception e) {
            model.addAttribute("message", "Có lỗi xảy ra khi xuất dữ liệu: " + e.getMessage());
        }

        return showExportForm(model);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadStudents() throws Exception {
        String filePath = "students.csv";
        File file = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }
}
