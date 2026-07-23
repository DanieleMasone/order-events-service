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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class UserGuideHtmlExportTest {

    private static final Path USER_GUIDE_SOURCE = Path.of("docs", "user-guide.md");
    private static final Path LANDING_PAGE_SOURCE = Path.of("src", "site", "index.html");
    private static final Path OUTPUT = Path.of("target", "generated-docs", "user-guide", "index.html");
    private static final Pattern STYLE_PATTERN = Pattern.compile("(?s)<style>\\s*(.*?)\\s*</style>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?s)<script>\\s*(.*?)\\s*</script>");

    @Test
    void exportsPublishedUserGuideHtmlFromMarkdownSource() throws Exception {
        String landingPage = Files.readString(LANDING_PAGE_SOURCE, StandardCharsets.UTF_8);
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

        String html = renderGuidePage(
                extract(landingPage, STYLE_PATTERN, "landing page styles"),
                extract(landingPage, SCRIPT_PATTERN, "theme script"),
                renderTableOfContents(headings),
                renderer.render(document)
        );

        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, html, StandardCharsets.UTF_8);

        assertThat(OUTPUT).exists().isRegularFile();
        assertThat(html)
                .contains("<title>User Guide | Order Events Service</title>")
                .contains("href=\"../\"")
                .contains("href=\"../openapi/\"")
                .contains("href=\"../jacoco/index.html\"")
                .contains("id=\"local-development\"")
                .contains("id=\"generated-documentation\"")
                .contains("docker compose up --build")
                .contains("localStorage")
                .doesNotContain("href=\"docs/user-guide.md\"")
                .doesNotContain("href=\"../docs/user-guide.md\"")
                .doesNotContain("href=\"./user-guide.md\"");
    }

    private static String withoutDocumentTitle(String markdown) {
        return markdown.replaceFirst("(?s)^#\\s+User Guide\\R+", "");
    }

    private static String extract(String html, Pattern pattern, String description) {
        Matcher matcher = pattern.matcher(html);
        assertThat(matcher.find()).as("Expected %s in src/site/index.html", description).isTrue();
        return matcher.group(1);
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

    private static String renderGuidePage(String landingStyles, String themeScript, String toc, String content) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>User Guide | Order Events Service</title>
                  <style>
                %s
                %s
                  </style>
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

                  <script>
                %s
                  </script>
                </body>
                </html>
                """.formatted(landingStyles, guideStyles(), toc, content, themeScript);
    }

    private static String guideStyles() {
        return """
                    .guide-hero {
                      padding-bottom: 2.5rem;
                    }

                    .guide-hero h1 {
                      max-width: none;
                    }

                    .guide-section {
                      padding-top: 0;
                    }

                    .guide-layout {
                      display: grid;
                      gap: 1rem;
                    }

                    .guide-toc,
                    .guide-content {
                      background: var(--panel);
                      border: 1px solid var(--line);
                      border-radius: var(--radius);
                      padding: 1rem;
                    }

                    .guide-toc {
                      align-self: start;
                    }

                    .guide-toc h2 {
                      font-size: 1.1rem;
                      margin-bottom: 0.75rem;
                    }

                    .guide-toc ol {
                      list-style: none;
                      margin: 0;
                      padding: 0;
                    }

                    .guide-toc li + li {
                      margin-top: 0.45rem;
                    }

                    .guide-toc .toc-level-3 {
                      padding-left: 1rem;
                    }

                    .guide-toc a {
                      color: var(--muted);
                      font-weight: 700;
                      text-decoration: none;
                    }

                    .guide-toc a:hover {
                      color: var(--accent-strong);
                      text-decoration: underline;
                    }

                    .guide-content {
                      min-width: 0;
                    }

                    .guide-content h2,
                    .guide-content h3 {
                      scroll-margin-top: 1.5rem;
                    }

                    .guide-content h2 {
                      font-size: clamp(1.35rem, 3vw, 1.8rem);
                      margin: 2.5rem 0 0.75rem;
                    }

                    .guide-content h2:first-child {
                      margin-top: 0;
                    }

                    .guide-content h3 {
                      font-size: 1.05rem;
                      margin: 1.6rem 0 0.45rem;
                    }

                    .guide-content p,
                    .guide-content li {
                      color: var(--muted);
                      max-width: 72ch;
                    }

                    .guide-content p + p,
                    .guide-content ul + p,
                    .guide-content ol + p,
                    .guide-content pre + p {
                      margin-top: 0.85rem;
                    }

                    .guide-content ul,
                    .guide-content ol {
                      margin: 0.75rem 0 0;
                      padding-left: 1.35rem;
                    }

                    .guide-content li + li {
                      margin-top: 0.35rem;
                    }

                    .guide-content pre {
                      margin: 0.85rem 0 0;
                    }

                    .guide-content pre code {
                      background: transparent;
                      border: 0;
                      color: inherit;
                      padding: 0;
                    }

                    .guide-content blockquote {
                      border-left: 4px solid var(--accent);
                      color: var(--muted);
                      margin: 1rem 0 0;
                      padding-left: 1rem;
                    }

                    @media (min-width: 900px) {
                      .guide-layout {
                        grid-template-columns: minmax(14rem, 0.32fr) minmax(0, 1fr);
                        align-items: start;
                      }

                      .guide-toc {
                        position: sticky;
                        top: 1rem;
                      }
                    }
                """;
    }

    private record GuideHeading(int level, String text, String id) {
    }
}
