package com.adit.exercise.document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;

@Slf4j
@Service
public class DocumentService {
    public ResponseEntity<Object> downloadDocument(HttpServletResponse response){
        try {
            String documentName = "Document.pdf";
            Map<String, String> fields = new HashMap<>();
            fields.put("NAME", "ABI");
            fields.put("AGE", "26");
            fields.put("GENDER", "M");
            fields.put("HOBBY", "BADMINTON");
            File file = new ClassPathResource("templates/document.docx").getFile();
            if(file.exists()) {
                XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
                for (XWPFParagraph paragraph : doc.getParagraphs()) {
                    String paragraphText = paragraph.getText();
                    if (!paragraphText.contains("${")) {
                        continue;
                    }

                    replaceText(paragraphText, paragraph, fields);
                }

                response.setHeader("Content-Disposition", "attachment;filename=".concat(documentName));
                response.setHeader("Content-Filename", documentName);
                response.setContentType("application/pdf");
                ServletOutputStream outputStream = response.getOutputStream();
                PdfConverter.getInstance().convert(doc, outputStream, PdfOptions.create());
                doc.close();
                outputStream.close();
                return ResponseEntity.accepted().build();
            }
        } catch (IOException e) {
            log.error("FAILED DOWNLOAD FILE ", e);
        }

        return ResponseEntity.badRequest().build();
    }

    private void replaceText(String paragraphText, XWPFParagraph paragraph, Map<String, String> fields) {
        for (Map.Entry<String, String> field : fields.entrySet()) {
            String find = "${" + field.getKey() + "}";
            if (!paragraphText.contains(find)) {
                continue;
            }

            doReplace(paragraph, find, field);
        }
    }

    private void doReplace(XWPFParagraph paragraph, String find, Map.Entry<String, String> field) {
        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String text = run.text();

            if (Objects.isNull(text) && text.isEmpty()) {
                continue;
            }

            if (text.contains("${") || (text.contains("$") && runs.get(i + 1).text().startsWith("{"))) {
                while (!text.contains("}")) {
                    text += runs.get(i + 1).text();
                    paragraph.removeRun(i + 1);
                }
                run.setText(text.contains(find) ? text.replace(find, field.getValue()) : text, 0);
            }
        }
    }
}
