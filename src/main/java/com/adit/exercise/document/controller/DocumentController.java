package com.adit.exercise.document.controller;

import com.adit.exercise.document.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/download")
    public void download(HttpServletResponse response) {
        documentService.downloadDocument(response);
    }
}
