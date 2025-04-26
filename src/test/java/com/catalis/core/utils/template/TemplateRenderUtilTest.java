package com.catalis.core.utils.template;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TemplateRenderUtil.
 */
public class TemplateRenderUtilTest {
    private static final String TEMPLATE_DIR = "templates";

    @BeforeAll
    static void setupTemplateDirectory() throws IOException {
        Path dir = Paths.get(TEMPLATE_DIR);
        if (Files.notExists(dir)) {
            Files.createDirectory(dir);
        }
        // Write a simple FreeMarker template for testing
        String templateContent = "<p>Hello ${name}!</p>";
        Files.write(dir.resolve("test.ftl"), templateContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testRenderTemplateToHtml() throws IOException, TemplateException {
        String html = TemplateRenderUtil.renderTemplateToHtml("test.ftl", Map.of("name", "World"));
        assertNotNull(html, "HTML output should not be null");
        assertTrue(html.contains("Hello World!"), "Rendered HTML should contain the correct greeting");
    }

    @Test
    void testRenderTemplateStringToHtml() throws IOException, TemplateException {
        // Create a template string
        String templateContent = "<p>Hello ${name}!</p>";

        // Create a data model
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "Template String");

        // Render the template string
        String html = TemplateRenderUtil.renderTemplateStringToHtml(templateContent, "string-template", dataModel);

        // Verify the HTML contains the expected content
        assertNotNull(html, "HTML output should not be null");
        assertTrue(html.contains("Hello Template String!"), "Rendered HTML should contain the correct greeting");
    }

    @Test
    void testRenderTemplateStringWithNullName() throws IOException, TemplateException {
        // Create a template string
        String templateContent = "<p>Hello ${name}!</p>";

        // Create a data model
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "Auto-named Template");

        // Render the template string with null name (should auto-generate a name)
        String html = TemplateRenderUtil.renderTemplateStringToHtml(templateContent, null, dataModel);

        // Verify the HTML contains the expected content
        assertNotNull(html, "HTML output should not be null");
        assertTrue(html.contains("Hello Auto-named Template!"), "Rendered HTML should contain the correct greeting");
    }

    @Test
    void testRenderHtmlToPdfProducesPdfHeader() throws Exception {
        String html = "<html><body><h1>Test PDF</h1></body></html>";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TemplateRenderUtil.renderHtmlToPdf(html, os, new TemplateRenderUtil.PdfOptions());
        byte[] pdfBytes = os.toByteArray();

        assertTrue(pdfBytes.length > 0, "PDF output should not be empty");
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header, "PDF header should start with '%PDF'");
    }

    @Test
    void testRenderTemplateToPdfProducesValidPdf() throws Exception {
        // Render using the test.ftl template and ensure PDF header
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TemplateRenderUtil.renderTemplateToPdf(
                "test.ftl",
                Map.of("name", "JUnit"),
                os,
                new TemplateRenderUtil.PdfOptions()
        );
        byte[] pdfBytes = os.toByteArray();

        assertTrue(pdfBytes.length > 0, "PDF output from template should not be empty");
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header, "PDF header should start with '%PDF'");
    }

    @Test
    void testRenderTemplateStringToPdfProducesValidPdf() throws Exception {
        // Create a template string
        String templateContent = "<html><body><h1>Hello ${name}!</h1></body></html>";

        // Create a data model
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "Template String PDF");

        // Render the template string to PDF
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TemplateRenderUtil.renderTemplateStringToPdf(
                templateContent,
                "string-template-pdf",
                dataModel,
                os,
                new TemplateRenderUtil.PdfOptions()
        );
        byte[] pdfBytes = os.toByteArray();

        // Verify the PDF was created correctly
        assertTrue(pdfBytes.length > 0, "PDF output from template string should not be empty");
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header, "PDF header should start with '%PDF'");
    }

    @Test
    void testRenderTemplateStringToPdfBytes() throws Exception {
        // Create a template string
        String templateContent = "<html><body><h1>Hello ${name}!</h1></body></html>";

        // Create a data model
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "Template String PDF Bytes");

        // Render the template string to PDF bytes
        byte[] pdfBytes = TemplateRenderUtil.renderTemplateStringToPdfBytes(
                templateContent,
                "string-template-pdf-bytes",
                dataModel
        );

        // Verify the PDF bytes were created correctly
        assertTrue(pdfBytes.length > 0, "PDF bytes from template string should not be empty");
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header, "PDF header should start with '%PDF'");
    }

    @Test
    void testRenderTemplateToPdfFile() throws Exception {
        // Create a temporary file for the PDF output
        File tempFile = File.createTempFile("test-pdf-file-", ".pdf");
        tempFile.deleteOnExit();

        // Render the template to a PDF file
        TemplateRenderUtil.renderTemplateToPdfFile(
                "test.ftl",
                Map.of("name", "PDF File"),
                tempFile.getAbsolutePath()
        );

        // Verify the PDF file was created and is not empty
        assertTrue(tempFile.exists(), "PDF file should exist");
        assertTrue(tempFile.length() > 0, "PDF file should not be empty");

        // Read the first few bytes to verify it's a PDF
        byte[] header = Files.readAllBytes(tempFile.toPath());
        String headerStr = new String(header, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", headerStr, "PDF file should start with '%PDF'");
    }

    @Test
    void testInvalidHtmlThrows() {
        // Provide blank HTML
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> TemplateRenderUtil.renderHtmlToPdf("   ", os, new TemplateRenderUtil.PdfOptions())
        );
        assertTrue(ex.getMessage().contains("HTML content is empty"));
    }
}
