package com.github.tuonome.orderevents;

import org.commonmark.node.Code;
import org.commonmark.node.Heading;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserGuideHtmlExportTest {

    private static final Path USER_GUIDE_SOURCE = Path.of("docs", "user-guide.md");
    private static final Path OUTPUT = Path.of("target", "generated-docs", "user-guide", "index.html");

    @Test
    void exportsPublishedUserGuideHtmlFromMarkdownSource() throws Exception {
        String markdown = withoutDocumentTitle(Files.readString(USER_GUIDE_SOURCE, StandardCharsets.UTF_8));

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        Map<Node, String> headingIds = new IdentityHashMap<>();
        List<GuideHeading> headings = collectHeadings(document, headingIds);

        HtmlRenderer renderer = HtmlRenderer.builder()
                .attributeProviderFactory(context -> (node, tagName, attributes) -> {
                    String id = headingIds.get(node);
                    if (id != null) {
                        attributes.put("id", id);
                    }
                })
                .build();

        String html = renderGuidePage(renderTableOfContents(headings), renderer.render(document));

        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, html, StandardCharsets.UTF_8);

        assertThat(OUTPUT).exists().isRegularFile();
        assertThat(html)
                .contains("<title>User Guide | Order Events Service</title>")
                .contains("href=\"../\"")
                .contains("href=\"../assets/site.css\"")
                .contains("src=\"../assets/site.js\"")
                .contains("href=\"../openapi/\"")
                .contains("href=\"../jacoco/index.html\"")
                .contains("id=\"local-development\"")
                .contains("id=\"generated-documentation\"")
                .contains("docker compose up --build")
                .doesNotContain("href=\"docs/user-guide.md\"")
                .doesNotContain("href=\"../docs/user-guide.md\"")
                .doesNotContain("href=\"./user-guide.md\"");
    }

    private static String withoutDocumentTitle(String markdown) {
        return markdown.replaceFirst("(?s)^#\\s+User Guide\\R+", "");
    }

    private static List<GuideHeading> collectHeadings(Node document, Map<Node, String> headingIds) {
        List<GuideHeading> headings = new ArrayList<>();
        Map<String, Integer> slugCounts = new LinkedHashMap<>();
        collectHeadings(document, headingIds, headings, slugCounts);
        return headings;
    }

    private static void collectHeadings(
            Node node,
            Map<Node, String> headingIds,
            List<GuideHeading> headings,
            Map<String, Integer> slugCounts
    ) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Heading heading && heading.getLevel() >= 2 && heading.getLevel() <= 3) {
                String text = collectPlainText(heading).trim();
                String slug = uniqueSlug(text, slugCounts);
                headingIds.put(heading, slug);
                headings.add(new GuideHeading(heading.getLevel(), text, slug));
            }
            collectHeadings(child, headingIds, headings, slugCounts);
        }
    }

    private static String collectPlainText(Node node) {
        StringBuilder text = new StringBuilder();
        collectPlainText(node, text);
        return text.toString();
    }

    private static void collectPlainText(Node node, StringBuilder text) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Text literal) {
                text.append(literal.getLiteral());
            } else if (child instanceof Code literal) {
                text.append(literal.getLiteral());
            } else if (child instanceof SoftLineBreak || child instanceof HardLineBreak) {
                text.append(' ');
            }
            collectPlainText(child, text);
        }
    }

    private static String uniqueSlug(String text, Map<String, Integer> slugCounts) {
        String base = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (base.isBlank()) {
            base = "section";
        }

        int count = slugCounts.merge(base, 1, Integer::sum);
        return count == 1 ? base : base + "-" + count;
    }

    private static String renderTableOfContents(List<GuideHeading> headings) {
        StringBuilder html = new StringBuilder();
        html.append("<ol>");
        for (GuideHeading heading : headings) {
            html.append("<li class=\"toc-level-")
                    .append(heading.level())
                    .append("\"><a href=\"#")
                    .append(heading.id())
                    .append("\">")
                    .append(escapeHtml(heading.text()))
                    .append("</a></li>");
        }
        html.append("</ol>");
        return html.toString();
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String renderGuidePage(String toc, String content) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="description" content="Local setup, API usage, generated documentation, and troubleshooting for the Order Events Service.">
                  <title>User Guide | Order Events Service</title>
                  <link rel="stylesheet" href="../assets/site.css">
                  <script src="../assets/site.js" defer></script>
                </head>
                <body>
                  <a class="skip-link" href="#main">Skip to content</a>

                  <header>
                    <div class="nav">
                      <a class="brand" href="../">Order Events Service <span>Spring Boot event-driven microservice</span></a>
                      <nav aria-label="Primary navigation">
                        <ul>
                          <li><a href="../">Overview</a></li>
                          <li><a href="../#architecture">Architecture</a></li>
                          <li><a href="./" aria-current="page">Guide</a></li>
                          <li><a href="../openapi/">API docs</a></li>
                          <li><a href="../jacoco/index.html">Coverage</a></li>
                          <li><a href="https://github.com/danielemasone/order-events-service">Repository</a></li>
                          <li><button class="theme-toggle" id="themeToggle" type="button" aria-pressed="false">Dark mode</button></li>
                        </ul>
                      </nav>
                    </div>
                  </header>

                  <main id="main">
                    <section class="guide-hero" aria-labelledby="guide-title">
                      <div class="inner">
                        <p class="eyebrow">Operational documentation</p>
                        <h1 id="guide-title">User Guide</h1>
                        <p class="lead">Practical setup, API usage, event flow, generated documentation, CI validation, and troubleshooting for the Order Events Service.</p>
                      </div>
                    </section>

                    <section class="guide-section" aria-labelledby="contents-title">
                      <div class="inner guide-layout">
                        <aside class="guide-toc" aria-labelledby="contents-title">
                          <h2 id="contents-title">Contents</h2>
                          %s
                        </aside>
                        <article class="guide-content">
                %s
                        </article>
                      </div>
                    </section>
                  </main>

                  <footer>
                    <div>Generated from <code>docs/user-guide.md</code> by Maven under <code>target/generated-docs/user-guide</code>.</div>
                  </footer>
                </body>
                </html>
                """.formatted(toc, content);
    }

    private record GuideHeading(int level, String text, String id) {
    }
}
