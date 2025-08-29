# lib-utils

A collection of utility classes for common operations in Java applications.

## Overview

The lib-utils library provides a set of powerful utilities to simplify common tasks in Java applications. The primary component is the `TemplateRenderUtil` class, which offers comprehensive template rendering capabilities using FreeMarker.

## Features

### Template Rendering (FreeMarker)

The `TemplateRenderUtil` class provides utilities for rendering FreeMarker templates to HTML, PDF, and image formats with extensive customization options. Key capabilities include:

- **Flexible Template Sources**: Render templates from files or strings
- **Multiple Output Formats**: Generate HTML, PDF, or image outputs
- **Direct HTML to PDF Conversion**: Convert HTML strings directly to PDF files or byte arrays
- **Customizable PDF Options**: Control page size, margins, fonts, and security
- **Performance Optimization**: Template caching for improved performance
- **Advanced Features**: Processing hooks, shared variables, and asynchronous rendering

### Tutorial Documentation

Detailed tutorials are available in the `tutorials` directory:

1. **Introduction to TemplateRenderUtil**: Basic concepts and usage
2. **Template Syntax and Structure**: FreeMarker template syntax guide
3. **PDF Customization Options**: Detailed PDF configuration options
4. **Advanced Features**: Template caching, hooks, async rendering, and more

## Table of Contents

- [Installation](#installation)
- [Quick Start Guide](#quick-start-guide)
- [Template Configuration](#template-configuration)
- [Rendering Templates to HTML](#rendering-templates-to-html)
- [Converting HTML to PDF](#converting-html-to-pdf)
- [PDF Customization Options](#pdf-customization-options)
- [Template Caching](#template-caching)
- [Template Validation](#template-validation)
- [HTML to Image Conversion](#html-to-image-conversion)
- [Template Processing Hooks](#template-processing-hooks)
- [Asynchronous Rendering](#asynchronous-rendering)
- [Security Features](#security-features)
- [Advanced Usage](#advanced-usage)
- [Error Handling](#error-handling)
- [Complete Examples](#complete-examples)

## Installation

To use the utilities in this library, simply add the lib-utils dependency to your Maven `pom.xml` file:

```xml
<!-- lib-utils library -->
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

The lib-utils library already includes all necessary dependencies:

- FreeMarker Template Engine (for template processing)
- Flying Saucer with OpenPDF (for PDF generation)
- SLF4J API (for logging)

No additional dependencies are required to use the functionality provided by this library.

## Template Configuration

### Setting Template Directory

Configure FreeMarker to load templates from a specific directory:

```java
// Set a file system directory for templates
TemplateRenderUtil.setTemplateDirectory("/path/to/templates");
```

### Using Multiple Template Sources

Configure FreeMarker to load templates from multiple sources:

```java
// Load templates from both classpath and file system
// Templates will be searched first in classpath, then in file system
TemplateRenderUtil.setClasspathAndFileTemplateLoaders("templates", "/path/to/external/templates");
```

### Adding Shared Variables

Make variables available to all templates:

```java
// Add company information available in all templates
TemplateRenderUtil.addSharedVariable("companyName", "Acme Corporation");
TemplateRenderUtil.addSharedVariable("companyLogo", "/images/logo.png");
TemplateRenderUtil.addSharedVariable("currentYear", java.time.Year.now().getValue());
```

### Setting Configuration Properties

Customize FreeMarker's behavior with configuration properties:

```java
// Create and set configuration properties
Properties props = new Properties();
props.setProperty("number_format", "0.##");
props.setProperty("date_format", "yyyy-MM-dd");
props.setProperty("locale", "en_US");

TemplateRenderUtil.setConfigurationProperties(props);
```

## Rendering Templates to HTML

### Rendering a Template File

```java
// Create a data model
Map<String, Object> dataModel = new HashMap<>();
dataModel.put("title", "Invoice #12345");
dataModel.put("customer", customerObject);
dataModel.put("items", itemsList);

// Render template to HTML
String html = TemplateRenderUtil.renderTemplateToHtml("invoice.ftl", dataModel);
```

### Rendering a Template String

```java
// Create a template string
String templateContent = "<html><body><h1>${title}</h1><p>Hello, ${name}!</p></body></html>";

// Create a data model
Map<String, Object> dataModel = new HashMap<>();
dataModel.put("title", "Welcome");
dataModel.put("name", "John Doe");

// Render template string to HTML
String html = TemplateRenderUtil.renderTemplateStringToHtml(templateContent, "welcome-template", dataModel);
```

### Saving Template Strings for Reuse

```java
// Create a template programmatically
String templateContent = "<html><body><h1>${title}</h1><p>${content}</p></body></html>";

// Save the template for future use
TemplateRenderUtil.saveTemplate(templateContent, "dynamic-template.ftl");

// Later, use the saved template
String html = TemplateRenderUtil.renderTemplateToHtml("dynamic-template.ftl", dataModel);
```

## Converting HTML to PDF

### Basic Conversion

```java
// Convert HTML to PDF with default options
TemplateRenderUtil.renderHtmlToPdf(html, outputStream, new TemplateRenderUtil.PdfOptions());

// Convert HTML to PDF file
TemplateRenderUtil.renderHtmlToPdfFile(html, "output.pdf");

// Convert HTML to PDF bytes
byte[] pdfBytes = TemplateRenderUtil.renderHtmlToPdfBytes(html);

// Convert HTML to PDF asynchronously
CompletableFuture<byte[]> pdfFuture = TemplateRenderUtil.renderHtmlToPdfBytesAsync(html);
pdfFuture.thenAccept(bytes -> {
    // Process the PDF bytes
});
```

### Direct Template to PDF Conversion

```java
// Render template directly to PDF file
TemplateRenderUtil.renderTemplateToPdfFile("invoice.ftl", dataModel, "invoice.pdf");

// Render template to PDF bytes
byte[] pdfBytes = TemplateRenderUtil.renderTemplateToPdfBytes("invoice.ftl", dataModel);
```

### Template String to PDF Conversion

```java
// Create a template string
String templateContent = "<html><body><h1>${title}</h1><p>${content}</p></body></html>";

// Create a data model
Map<String, Object> dataModel = new HashMap<>();
dataModel.put("title", "Generated PDF");
dataModel.put("content", "This PDF was generated from a template string.");

// Render template string directly to PDF file
TemplateRenderUtil.renderTemplateStringToPdfFile(
    templateContent,
    "inline-template",
    dataModel,
    "template-string-output.pdf"
);

// Render template string to PDF bytes (useful for web downloads)
byte[] pdfBytes = TemplateRenderUtil.renderTemplateStringToPdfBytes(
    templateContent,
    "inline-template",
    dataModel
);
```

## PDF Customization Options

The `PdfOptions` class provides customization for PDF output:

```java
// Create PDF options with custom settings
TemplateRenderUtil.PdfOptions options = new TemplateRenderUtil.PdfOptions()
    // Set page size (A4, LETTER, LEGAL, A3)
    .withPageSize(TemplateRenderUtil.PdfOptions.PageSize.LETTER)

    // Set margins in points (1/72 inch)
    .withMargins(72, 72, 72, 72)  // 1 inch margins on all sides

    // Set base URI for resolving relative links
    .withBaseUri("classpath:static/")

    // Set font directory for custom fonts
    .withFontDirectory("/path/to/fonts")

    // Set default font
    .withDefaultFont("Arial")

    // Add PDF bookmarks (outline entries)
    .withBookmark("Chapter 1", "1")
    .withChildBookmark("Section 1.1", "1")
    .withChildBookmark("Section 1.2", "1")
    .withBookmark("Chapter 2", "2");

// Use the options when converting to PDF
TemplateRenderUtil.renderTemplateToPdfFile("invoice.ftl", dataModel, "custom-output.pdf", options);

// Or use with template string
TemplateRenderUtil.renderTemplateStringToPdfFile(
    templateContent,
    "inline-template",
    dataModel,
    "custom-string-output.pdf",
    options
);
```

## Template Caching

Improve performance by caching templates:

```java
// Enable template caching
TemplateRenderUtil.setTemplateCachingEnabled(true);

// Set maximum cache size (number of templates)
TemplateRenderUtil.setTemplateCacheMaxSize(100);

// Clear the template cache when needed
TemplateRenderUtil.clearTemplateCache();
```

## Template Validation

Validate templates before using them:

```java
// Validate a template string
String templateContent = "<p>Hello ${name}!</p>";
List<String> errors = TemplateRenderUtil.validateTemplate(templateContent);
if (errors.isEmpty()) {
    System.out.println("Template is valid!");
} else {
    System.out.println("Template has errors: " + errors);
}

// Validate a template file
List<String> fileErrors = TemplateRenderUtil.validateTemplateFile("invoice.ftl");
if (fileErrors.isEmpty()) {
    System.out.println("Template file is valid!");
} else {
    System.out.println("Template file has errors: " + fileErrors);
}
```

## HTML to Image Conversion

Convert HTML content or templates to images:

```java
// Convert HTML string to image
String html = "<html><body><h1 style='color:blue'>Hello World</h1></body></html>";
byte[] pngBytes = TemplateRenderUtil.renderHtmlToImage(html, 800, 600, "png");

// Save the image to a file
TemplateRenderUtil.saveImageToFile(pngBytes, "output.png");

// Render a template directly to an image
byte[] templateImageBytes = TemplateRenderUtil.renderTemplateToImage(
    "certificate.ftl",
    dataModel,
    1024, 768,
    "jpg"
);

// Render a template string to an image
String templateContent = "<html><body><h1>${title}</h1><p>${content}</p></body></html>";
byte[] templateStringImageBytes = TemplateRenderUtil.renderTemplateStringToImage(
    templateContent,
    "inline-template",
    dataModel,
    800, 600,
    "png"
);
```

## Template Processing Hooks

Add pre-processing and post-processing hooks to modify templates and rendered HTML:

```java
// Add a pre-processor to modify template content before rendering
TemplateRenderUtil.setTemplatePreProcessor((content, model) -> {
    // Add a header to all templates
    return "<div class='header'>Company Header</div>\n" + content;
});

// Add a post-processor to modify rendered HTML
TemplateRenderUtil.setTemplatePostProcessor((html, model) -> {
    // Add analytics code to all rendered pages
    return html + "\n<script>trackPageView();</script>";
});

// Clear processors when no longer needed
TemplateRenderUtil.setTemplatePreProcessor(null);
TemplateRenderUtil.setTemplatePostProcessor(null);
```

## Asynchronous Rendering

Render templates asynchronously for improved performance:

```java
// Render template to HTML asynchronously
CompletableFuture<String> htmlFuture = TemplateRenderUtil.renderTemplateToHtmlAsync(
    "report.ftl",
    dataModel
);

// Process the result when it's ready
htmlFuture.thenAccept(html -> {
    System.out.println("HTML rendering completed, length: " + html.length());
});

// Render template to PDF asynchronously
CompletableFuture<byte[]> pdfFuture = TemplateRenderUtil.renderTemplateToPdfBytesAsync(
    "invoice.ftl",
    invoiceData,
    new TemplateRenderUtil.PdfOptions()
);

// Process the PDF when it's ready
pdfFuture.thenAccept(pdfBytes -> {
    System.out.println("PDF rendering completed, size: " + pdfBytes.length + " bytes");
    // Save the PDF or send it to the client
});

// Configure the thread pool size for async operations
TemplateRenderUtil.setAsyncThreadPoolSize(10);

// Shutdown the thread pool when no longer needed (e.g., on application shutdown)
TemplateRenderUtil.shutdownAsyncThreadPool();
```

## Security Features

Protect your PDF documents with passwords and permissions:

```java
// Create PDF options with security settings
PdfRenderOptions options = new PdfRenderOptions()
    // Set user password (required to open the document)
    .setUserPassword("user123")

    // Set owner password (required to change the document)
    .setOwnerPassword("owner456")

    // Set permissions
    .setAllowPrinting(true)   // Allow/disallow printing
    .setAllowCopy(false);     // Allow/disallow copying content

// Use the options when converting to PDF
TemplateRenderUtil.renderTemplateToPdf("confidential-report.ftl", dataModel, "protected-report.pdf", options);
```

## Advanced Usage

### Saving Templates

```java
// Create a template programmatically
String templateContent = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head><title>${title}</title></head>\n" +
                        "<body>\n" +
                        "<h1>${title}</h1>\n" +
                        "<p>${content}</p>\n" +
                        "</body>\n" +
                        "</html>";

// Save the template for future use
TemplateRenderUtil.saveTemplate(templateContent, "dynamic-template.ftl");
```

### Combining Multiple Features

```java
// Set up template configuration
TemplateRenderUtil.setClasspathAndFileTemplateLoaders("templates", "./custom-templates");
TemplateRenderUtil.addSharedVariable("company", companyInfo);

// Create custom PDF options
PdfRenderOptions options = new PdfRenderOptions(PdfRenderOptions.PageSize.A4)
    .setMargins(50)
    .setUserPassword("secret")
    .setAllowPrinting(true)
    .setAllowCopy(false);

// Render template directly to PDF with all options
TemplateRenderUtil.renderTemplateToPdf("reports/quarterly.ftl", reportData, "Q2-2023-Report.pdf", options);
```

## Error Handling

The utility provides detailed error handling:

```java
try {
    // Attempt to render a template
    String html = TemplateRenderUtil.renderTemplateToHtml("invoice.ftl", dataModel);
    TemplateRenderUtil.renderTemplateToPdfFile("invoice.ftl", dataModel, "invoice.pdf");
} catch (IOException e) {
    // Handle file access errors
    logger.error("Could not access template or output file", e);
} catch (TemplateException e) {
    // Handle template processing errors
    logger.error("Error in template syntax or processing", e);
} catch (DocumentException e) {
    // Handle PDF creation errors
    logger.error("Error creating PDF document", e);
}
```

## Complete Examples

### Invoice Generation Example

```java
// Create invoice data
Map<String, Object> invoice = new HashMap<>();
invoice.put("invoiceNumber", "INV-2023-001");
invoice.put("date", new Date());
invoice.put("dueDate", new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));

Map<String, Object> customer = new HashMap<>();
customer.put("name", "John Doe");
customer.put("email", "john.doe@example.com");
customer.put("address", "123 Main St, Anytown, USA");
invoice.put("customer", customer);

List<Map<String, Object>> items = new ArrayList<>();
items.add(createItem("Website Design", 1, 1200.00));
items.add(createItem("Hosting (1 year)", 1, 300.00));
items.add(createItem("Domain Registration", 2, 15.00));
invoice.put("items", items);

// Calculate totals
double subtotal = items.stream()
        .mapToDouble(item -> (double)item.get("quantity") * (double)item.get("price"))
        .sum();
invoice.put("subtotal", subtotal);
invoice.put("tax", subtotal * 0.1); // 10% tax
invoice.put("total", subtotal * 1.1);

// Set up PDF options
TemplateRenderUtil.PdfOptions options = new TemplateRenderUtil.PdfOptions()
        .withPageSize(TemplateRenderUtil.PdfOptions.PageSize.A4)
        .withMargins(36, 36, 36, 36); // 0.5 inch margins

// Generate the invoice PDF
try {
    TemplateRenderUtil.renderTemplateToPdfFile("invoice.ftl", invoice,
            "Invoice-" + invoice.get("invoiceNumber") + ".pdf", options);
    System.out.println("Invoice generated successfully!");
} catch (Exception e) {
    System.err.println("Failed to generate invoice: " + e.getMessage());
    e.printStackTrace();
}

// Helper method to create an item
private static Map<String, Object> createItem(String description, int quantity, double price) {
    Map<String, Object> item = new HashMap<>();
    item.put("description", description);
    item.put("quantity", quantity);
    item.put("price", price);
    item.put("amount", quantity * price);
    return item;
}
```

### Dynamic Template Generation Example

```java
// Create a template string dynamically
StringBuilder templateBuilder = new StringBuilder();
templateBuilder.append("<!DOCTYPE html>\n");
templateBuilder.append("<html>\n");
templateBuilder.append("<head>\n");
templateBuilder.append("  <title>${title}</title>\n");
templateBuilder.append("  <style>\n");
templateBuilder.append("    body { font-family: Arial, sans-serif; margin: 40px; }\n");
templateBuilder.append("    h1 { color: #333366; }\n");
templateBuilder.append("    .content { border: 1px solid #dddddd; padding: 20px; }\n");
templateBuilder.append("    .footer { margin-top: 30px; font-size: 0.8em; color: #666666; }\n");
templateBuilder.append("  </style>\n");
templateBuilder.append("</head>\n");
templateBuilder.append("<body>\n");
templateBuilder.append("  <h1>${title}</h1>\n");
templateBuilder.append("  <div class=\"content\">\n");
templateBuilder.append("    <p>${content}</p>\n");
templateBuilder.append("    <#if items?has_content>\n");
templateBuilder.append("    <ul>\n");
templateBuilder.append("      <#list items as item>\n");
templateBuilder.append("      <li>${item}</li>\n");
templateBuilder.append("      </#list>\n");
templateBuilder.append("    </ul>\n");
templateBuilder.append("    </#if>\n");
templateBuilder.append("  </div>\n");
templateBuilder.append("  <div class=\"footer\">\n");
templateBuilder.append("    Generated on: ${.now?string(\"yyyy-MM-dd HH:mm:ss\")}\n");
templateBuilder.append("  </div>\n");
templateBuilder.append("</body>\n");
templateBuilder.append("</html>");

String templateContent = templateBuilder.toString();

// Create a data model
Map<String, Object> dataModel = new HashMap<>();
dataModel.put("title", "Dynamic Template Example");
dataModel.put("content", "This document was generated from a dynamically created template string.");

List<String> items = new ArrayList<>();
items.add("Item 1");
items.add("Item 2");
items.add("Item 3");
dataModel.put("items", items);

// Render the template string to PDF
try {
    TemplateRenderUtil.renderTemplateStringToPdfFile(
            templateContent,
            "dynamic-template",
            dataModel,
            "dynamic-template-output.pdf"
    );
    System.out.println("Dynamic template PDF generated successfully!");
} catch (Exception e) {
    System.err.println("Failed to generate PDF: " + e.getMessage());
    e.printStackTrace();
}
```

### Annotations

#### FilterableId

The `@FilterableId` annotation is used to mark ID fields that should be included in filtering operations. By default, fields that end with "Id" are excluded from filter parameters, but when a field is annotated with `@FilterableId`, it will be included in the filters with specific behavior.

```java
public class UserFilterDTO {
    @FilterableId
    private Long customerId;  // Will be included in filters with exact matching

    private Long accountId;   // Will be excluded from filters

    private String name;      // Regular field with full filtering capabilities
}
```

##### Behavior

- In OpenAPI/Swagger documentation:
  - The field will appear as a query parameter
  - No range parameters will be generated for the field

- In runtime filtering:
  - The field will be included in filtering operations
  - Only exact matching will be used (no LIKE or range operations)
  - Range filters will be ignored for this field
