package com.catalis.core.utils.template;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility for rendering FreeMarker templates to XHTML and PDF using Flying Saucer.
 * Ensures well-formed XHTML, supports classpath and filesystem loaders,
 * custom PDF options, and font embedding.
 *
 * This utility supports rendering templates from both paths and strings.
 */
public class TemplateRenderUtil {
    private static final Logger logger = LoggerFactory.getLogger(TemplateRenderUtil.class);
    private static final Configuration freemarkerConfig = createFreemarkerConfig();

    private static Configuration createFreemarkerConfig() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Multi-loader: first classpath:/templates, then ./templates
        ClassTemplateLoader ctl = new ClassTemplateLoader(
                Thread.currentThread().getContextClassLoader(), "/templates");
        try {
            FileTemplateLoader ftl = new FileTemplateLoader(new File("templates"));
            TemplateLoader[] loaders = new TemplateLoader[]{ctl, ftl};
            cfg.setTemplateLoader(new MultiTemplateLoader(loaders));
        } catch (IOException e) {
            logger.warn("Filesystem loader not available, falling back to classpath only", e);
            cfg.setTemplateLoader(ctl);
        }

        return cfg;
    }

    /**
     * Sets the directory where template files are located.
     *
     * @param templateDir the directory containing template files
     * @throws IOException if the directory cannot be accessed
     */
    public static void setTemplateDirectory(String templateDir) throws IOException {
        File dir = new File(templateDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Template directory does not exist: " + templateDir);
        }
        freemarkerConfig.setDirectoryForTemplateLoading(dir);
        logger.info("FreeMarker configured to load templates from directory: {}", templateDir);
    }

    /**
     * Configures FreeMarker to load templates from both classpath and file system.
     * Templates will be searched first in the classpath, then in the file system.
     *
     * @param classpathPrefix prefix for classpath resources (e.g., "templates")
     * @param fileSystemDir directory for file system resources
     * @throws IOException if the file system directory cannot be accessed
     */
    public static void setClasspathAndFileTemplateLoaders(String classpathPrefix, String fileSystemDir) throws IOException {
        if (classpathPrefix == null || classpathPrefix.trim().isEmpty()) {
            classpathPrefix = "templates";
        }

        if (fileSystemDir == null || fileSystemDir.trim().isEmpty()) {
            fileSystemDir = "templates";
        }

        ClassTemplateLoader classLoader = new ClassTemplateLoader(
                Thread.currentThread().getContextClassLoader(), classpathPrefix);

        File dir = new File(fileSystemDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.warn("Could not create template directory: {}", fileSystemDir);
                // Continue with just the classpath loader
                freemarkerConfig.setTemplateLoader(classLoader);
                logger.info("FreeMarker configured to load templates from classpath:{} only", classpathPrefix);
                return;
            }
        }

        if (!dir.isDirectory()) {
            throw new IOException("Template path is not a directory: " + fileSystemDir);
        }

        try {
            FileTemplateLoader fileLoader = new FileTemplateLoader(dir);
            TemplateLoader[] loaders = new TemplateLoader[]{classLoader, fileLoader};
            freemarkerConfig.setTemplateLoader(new MultiTemplateLoader(loaders));
            logger.info("FreeMarker configured to load templates from classpath:{} and directory:{}",
                      classpathPrefix, fileSystemDir);
        } catch (IOException e) {
            // If file loader fails, continue with just the classpath loader
            freemarkerConfig.setTemplateLoader(classLoader);
            logger.info("FreeMarker configured to load templates from classpath:{} only", classpathPrefix);
            logger.warn("Could not configure file system template loader", e);
        }
    }

    /**
     * Renders a FreeMarker template to an XHTML string.
     * @param templateName path within loaders, e.g., "invoice.ftl"
     * @param dataModel the data model to use for rendering
     * @return the rendered HTML as a string
     * @throws IOException if the template cannot be read
     * @throws TemplateException if the template cannot be processed
     */
    public static String renderTemplateToHtml(String templateName, Map<String, Object> dataModel)
            throws IOException, TemplateException {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        if (dataModel == null) {
            dataModel = new HashMap<>();
        }

        try {
            Template tpl = freemarkerConfig.getTemplate(templateName);
            try (StringWriter out = new StringWriter()) {
                tpl.process(dataModel, out);
                return out.toString();
            }
        } catch (IOException e) {
            logger.error("Failed to load template: {}", templateName, e);
            throw new IOException("Failed to load template: " + templateName, e);
        } catch (TemplateException e) {
            logger.error("Failed to process template: {}", templateName, e);
            throw e;
        }
    }

    /**
     * Renders a FreeMarker template string (not a file) to an HTML string.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @return the rendered HTML as a string
     * @throws IOException if an I/O error occurs
     * @throws TemplateException if the template cannot be processed
     * @throws IllegalArgumentException if templateContent is null or empty
     */
    public static String renderTemplateStringToHtml(String templateContent, String templateName, Map<String, Object> dataModel)
            throws IOException, TemplateException {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Template content cannot be null or empty");
        }

        if (templateName == null || templateName.trim().isEmpty()) {
            templateName = "inline-template-" + System.currentTimeMillis();
        }

        if (dataModel == null) {
            dataModel = new HashMap<>();
        }

        try {
            Template template = new Template(templateName, new StringReader(templateContent), freemarkerConfig);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (TemplateException e) {
            logger.error("Failed to process template string: {}", templateName, e);
            throw e;
        } catch (IOException e) {
            logger.error("I/O error processing template string: {}", templateName, e);
            throw e;
        }
    }

    /**
     * Saves a template string to a file in the templates directory.
     *
     * @param templateContent the content of the template
     * @param templateName the name to save the template as
     * @throws IOException if the file cannot be written
     */
    public static void saveTemplate(String templateContent, String templateName) throws IOException {
        // Create a default templates directory if it doesn't exist
        File templateDir = new File("templates");
        if (!templateDir.exists()) {
            if (!templateDir.mkdirs()) {
                throw new IOException("Could not create templates directory: " + templateDir);
            }
        }

        Path templatePath = Paths.get(templateDir.getPath(), templateName);
        Files.write(templatePath, templateContent.getBytes(StandardCharsets.UTF_8));
        logger.info("Template saved to: {}", templatePath);
    }

    /**
     * Renders HTML/XHTML content to PDF using custom options.
     */
    public static void renderHtmlToPdf(String htmlContent, OutputStream os, PdfOptions options) throws Exception {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException("HTML content is empty");
        }

        // Ensure well-formed XHTML and inject page CSS
        String xhtml = ensureXhtmlDocument(htmlContent);
        xhtml = injectPageCss(xhtml, options);

        ITextRenderer renderer = new ITextRenderer();
        configureFonts(renderer, options);

        if (options.getBaseUri() != null) {
            ITextUserAgent ua = new ITextUserAgent(renderer.getOutputDevice());
            ua.setSharedContext(renderer.getSharedContext());
            ua.setBaseURL(options.getBaseUri());
            renderer.getSharedContext().setUserAgentCallback(ua);
            renderer.setDocumentFromString(xhtml, options.getBaseUri());
        } else {
            renderer.setDocumentFromString(xhtml);
        }

        renderer.layout();
        renderer.createPDF(os);
        os.flush();
    }

    /**
     * Renders a FreeMarker template directly to PDF.
     *
     * @param templateName the name of the template file
     * @param dataModel the data model to use for rendering
     * @param os the output stream to write the PDF to
     * @param options custom PDF rendering options
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateToPdf(String templateName, Map<String, Object> dataModel,
                                           OutputStream os, PdfOptions options) throws Exception {
        String html = renderTemplateToHtml(templateName, dataModel);
        renderHtmlToPdf(html, os, options);
    }

    /**
     * Renders a FreeMarker template string directly to PDF.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @param os the output stream to write the PDF to
     * @param options custom PDF rendering options
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateStringToPdf(String templateContent, String templateName,
                                                Map<String, Object> dataModel,
                                                OutputStream os, PdfOptions options) throws Exception {
        String html = renderTemplateStringToHtml(templateContent, templateName, dataModel);
        renderHtmlToPdf(html, os, options);
    }

    /**
     * Renders a FreeMarker template string directly to PDF with default options.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @param os the output stream to write the PDF to
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateStringToPdf(String templateContent, String templateName,
                                                Map<String, Object> dataModel,
                                                OutputStream os) throws Exception {
        renderTemplateStringToPdf(templateContent, templateName, dataModel, os, new PdfOptions());
    }

    /**
     * Renders a FreeMarker template to a PDF file.
     *
     * @param templateName the name of the template file
     * @param dataModel the data model to use for rendering
     * @param outputPath the path where the PDF file will be saved
     * @param options custom PDF rendering options
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateToPdfFile(String templateName, Map<String, Object> dataModel,
                                              String outputPath, PdfOptions options) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            renderTemplateToPdf(templateName, dataModel, fos, options);
            logger.info("PDF created successfully at: {}", outputPath);
        }
    }

    /**
     * Renders a FreeMarker template to a PDF file with default options.
     *
     * @param templateName the name of the template file
     * @param dataModel the data model to use for rendering
     * @param outputPath the path where the PDF file will be saved
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateToPdfFile(String templateName, Map<String, Object> dataModel,
                                              String outputPath) throws Exception {
        renderTemplateToPdfFile(templateName, dataModel, outputPath, new PdfOptions());
    }

    /**
     * Renders a FreeMarker template string to a PDF file.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @param outputPath the path where the PDF file will be saved
     * @param options custom PDF rendering options
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateStringToPdfFile(String templateContent, String templateName,
                                                    Map<String, Object> dataModel,
                                                    String outputPath, PdfOptions options) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            renderTemplateStringToPdf(templateContent, templateName, dataModel, fos, options);
            logger.info("PDF created successfully at: {}", outputPath);
        }
    }

    /**
     * Renders a FreeMarker template string to a PDF file with default options.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @param outputPath the path where the PDF file will be saved
     * @throws Exception if an error occurs during rendering
     */
    public static void renderTemplateStringToPdfFile(String templateContent, String templateName,
                                                    Map<String, Object> dataModel,
                                                    String outputPath) throws Exception {
        renderTemplateStringToPdfFile(templateContent, templateName, dataModel, outputPath, new PdfOptions());
    }

    /**
     * Renders a FreeMarker template to a PDF and returns it as a byte array.
     *
     * @param templateName the name of the template file
     * @param dataModel the data model to use for rendering
     * @param options custom PDF rendering options
     * @return byte array containing the PDF data
     * @throws Exception if an error occurs during rendering
     */
    public static byte[] renderTemplateToPdfBytes(String templateName, Map<String, Object> dataModel,
                                                 PdfOptions options) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            renderTemplateToPdf(templateName, dataModel, baos, options);
            return baos.toByteArray();
        }
    }

    /**
     * Renders a FreeMarker template to a PDF and returns it as a byte array with default options.
     *
     * @param templateName the name of the template file
     * @param dataModel the data model to use for rendering
     * @return byte array containing the PDF data
     * @throws Exception if an error occurs during rendering
     */
    public static byte[] renderTemplateToPdfBytes(String templateName, Map<String, Object> dataModel) throws Exception {
        return renderTemplateToPdfBytes(templateName, dataModel, new PdfOptions());
    }

    /**
     * Renders a FreeMarker template string to a PDF and returns it as a byte array.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @param options custom PDF rendering options
     * @return byte array containing the PDF data
     * @throws Exception if an error occurs during rendering
     */
    public static byte[] renderTemplateStringToPdfBytes(String templateContent, String templateName,
                                                       Map<String, Object> dataModel,
                                                       PdfOptions options) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            renderTemplateStringToPdf(templateContent, templateName, dataModel, baos, options);
            return baos.toByteArray();
        }
    }

    /**
     * Renders a FreeMarker template string to a PDF and returns it as a byte array with default options.
     *
     * @param templateContent the template content as a string
     * @param templateName a name for the template (used for error reporting)
     * @param dataModel the data model to use for rendering
     * @return byte array containing the PDF data
     * @throws Exception if an error occurs during rendering
     */
    public static byte[] renderTemplateStringToPdfBytes(String templateContent, String templateName,
                                                       Map<String, Object> dataModel) throws Exception {
        return renderTemplateStringToPdfBytes(templateContent, templateName, dataModel, new PdfOptions());
    }

    private static String ensureXhtmlDocument(String html) {
        String trimmed = html.trim().toLowerCase();
        if (!trimmed.startsWith("<!doctype")) {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \n");
            sb.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
            sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
            sb.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
            sb.append("</head><body>");
            sb.append(html);
            sb.append("</body></html>");
            return sb.toString();
        }
        return html;
    }

    private static String injectPageCss(String html, PdfOptions opts) {
        StringBuilder css = new StringBuilder();
        css.append("<style> @page { size: ")
                .append(opts.pageSize.name().toLowerCase())
                .append("; margin: ")
                .append(opts.marginTop).append("pt ")
                .append(opts.marginRight).append("pt ")
                .append(opts.marginBottom).append("pt ")
                .append(opts.marginLeft).append("pt; }");
        if (opts.getDefaultFont() != null) {
            css.append(" body { font-family: '")
                    .append(opts.getDefaultFont())
                    .append("'; }");
        }
        css.append(" </style>");

        int idx = html.indexOf("</head>");
        if (idx > -1) {
            return html.substring(0, idx) + css + html.substring(idx);
        }
        return css + html;
    }

    private static void configureFonts(ITextRenderer renderer, PdfOptions opts) {
        if (opts.getFontDir() != null) {
            try {
                org.xhtmlrenderer.pdf.ITextFontResolver fr = renderer.getFontResolver();
                File dir = new File(opts.getFontDir());
                if (dir.isDirectory()) {
                    for (File f : dir.listFiles()) {
                        String name = f.getName().toLowerCase();
                        if (name.endsWith(".ttf") || name.endsWith(".otf")) {
                            fr.addFont(f.getAbsolutePath(), true);
                            logger.debug("Loaded font: {}", f.getName());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Error loading fonts from {}", opts.getFontDir(), e);
            }
        }
    }

    /**
     * Options for PDF rendering.
     */
    public static class PdfOptions {
        private String baseUri;
        private String fontDir;
        private String defaultFont;
        private PageSize pageSize = PageSize.A4;
        private float marginTop = 36, marginRight = 36, marginBottom = 36, marginLeft = 36;

        public PdfOptions withBaseUri(String uri) { this.baseUri = uri; return this; }
        public PdfOptions withFontDirectory(String dir) { this.fontDir = dir; return this; }
        public PdfOptions withDefaultFont(String fontName) { this.defaultFont = fontName; return this; }
        public PdfOptions withPageSize(PageSize sz) { this.pageSize = sz; return this; }
        public PdfOptions withMargins(float top, float right, float bottom, float left) {
            this.marginTop = top; this.marginRight = right;
            this.marginBottom = bottom; this.marginLeft = left;
            return this;
        }

        public enum PageSize { A4, LETTER, LEGAL, A3 }

        // getters for internal use
        private String getBaseUri() { return baseUri; }
        private String getFontDir() { return fontDir; }
        private String getDefaultFont() { return defaultFont; }
    }
}
