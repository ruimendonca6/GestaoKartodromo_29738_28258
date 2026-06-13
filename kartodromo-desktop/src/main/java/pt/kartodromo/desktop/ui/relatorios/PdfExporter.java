package pt.kartodromo.desktop.ui.relatorios;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import pt.kartodromo.core.dal.ClienteDao;
import pt.kartodromo.core.dal.CorridaDao;
import pt.kartodromo.core.dal.ReservaDao;
import pt.kartodromo.core.model.Cliente;
import pt.kartodromo.core.model.Corrida;
import pt.kartodromo.core.model.Reserva;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    private static final PDType1Font FONT_BOLD   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 16f;
    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_NOW = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ------------------------------------------------------------------ public API

    public void exportarClientes(File destino) throws Exception {
        List<Cliente> lista = new ClienteDao().findAll();
        String[] cabecalho = {"Nome", "Email", "Data Nascimento", "Nivel Experiencia"};
        String[][] linhas = lista.stream().map(c -> new String[]{
                safe(c.getNome()),
                safe(c.getEmail()),
                c.getDataNascimento() != null ? c.getDataNascimento().toString() : "",
                String.valueOf(c.getNivelExperiencia())
        }).toArray(String[][]::new);
        escreverRelatorio("Relatorio de Clientes", cabecalho, linhas, null, destino);
    }

    public void exportarReservas(File destino) throws Exception {
        List<Reserva> lista = new ReservaDao().findAll();
        String[] cabecalho = {"Cliente", "Kart #", "Pista", "Inicio", "Fim", "Estado"};
        String[][] linhas = lista.stream().map(r -> new String[]{
                safe(r.getCliente().getNome()),
                String.valueOf(r.getKart().getNumero()),
                safe(r.getPistaNome()),
                r.getDataHoraInicio().format(FMT_DT),
                r.getDataHoraFim().format(FMT_DT),
                safe(r.getEstado().name())
        }).toArray(String[][]::new);
        String rodape = "Total de reservas: " + lista.size();
        escreverRelatorio("Relatorio de Reservas", cabecalho, linhas, rodape, destino);
    }

    public void exportarCorridas(File destino) throws Exception {
        List<Corrida> lista = new CorridaDao().findAll();
        String[] cabecalho = {"Data/Hora", "Layout", "Categoria", "Duracao (min)", "Vagas", "Cliente"};
        String[][] linhas = lista.stream().map(c -> new String[]{
                c.getDataHoraInicio().format(FMT_DT),
                safe(c.getLayoutNome()),
                safe(c.getCategoria().getDescricao()),
                String.valueOf(c.getDuracaoMinutos()),
                String.valueOf(c.getVagasMaximas()),
                safe(c.getCliente().getNome())
        }).toArray(String[][]::new);
        String rodape = "Total de corridas: " + lista.size();
        escreverRelatorio("Relatorio de Corridas", cabecalho, linhas, rodape, destino);
    }

    public void exportarReceita(File destino) throws Exception {
        List<Corrida> lista = new CorridaDao().findAll();
        String[] cabecalho = {"Data/Hora", "Layout", "Categoria", "Preco Base (EUR)", "Vagas", "Receita (EUR)"};
        BigDecimal totalReceita = BigDecimal.ZERO;
        String[][] linhas = new String[lista.size()][];
        for (int i = 0; i < lista.size(); i++) {
            Corrida c = lista.get(i);
            BigDecimal preco = c.getCategoria().getPrecoBase();
            BigDecimal receita = preco.multiply(BigDecimal.valueOf(c.getVagasMaximas()));
            totalReceita = totalReceita.add(receita);
            linhas[i] = new String[]{
                    c.getDataHoraInicio().format(FMT_DT),
                    safe(c.getLayoutNome()),
                    safe(c.getCategoria().getDescricao()),
                    String.format("%.2f", preco),
                    String.valueOf(c.getVagasMaximas()),
                    String.format("%.2f", receita)
            };
        }
        String rodape = "Receita Total: " + String.format("%.2f EUR", totalReceita);
        escreverRelatorio("Relatorio de Receita", cabecalho, linhas, rodape, destino);
    }

    // ------------------------------------------------------------------ core renderer

    private void escreverRelatorio(
            String titulo,
            String[] cabecalho,
            String[][] linhas,
            String rodape,
            File destino
    ) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PageWriter pw = new PageWriter(doc, titulo);

            // subheader: data de geracao
            pw.writeLine("Gerado em: " + LocalDateTime.now().format(FMT_NOW), FONT_NORMAL, 9, new float[]{1f});

            pw.newLine();

            // cabecalho da tabela
            pw.writeRow(cabecalho, FONT_BOLD, 9, true);

            // linhas
            boolean alt = false;
            for (String[] row : linhas) {
                pw.writeRow(row, FONT_NORMAL, 8, alt);
                alt = !alt;
            }

            // rodape
            if (rodape != null) {
                pw.newLine();
                pw.writeLine(rodape, FONT_BOLD, 10, new float[]{1f});
            }

            pw.finish();
            doc.save(destino);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------------ inner page writer

    private static class PageWriter {
        private final PDDocument doc;
        private final String titulo;
        private PDPage page;
        private PDPageContentStream cs;
        private float y;
        private int pageNum = 1;

        PageWriter(PDDocument doc, String titulo) throws Exception {
            this.doc = doc;
            this.titulo = titulo;
            newPage();
        }

        private void newPage() throws Exception {
            if (cs != null) cs.close();
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - MARGIN;
            drawPageHeader();
        }

        private void drawPageHeader() throws Exception {
            // titulo
            cs.beginText();
            cs.setFont(FONT_BOLD, 16);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(titulo);
            cs.endText();
            y -= 22;

            // linha separadora
            cs.setLineWidth(1.5f);
            cs.moveTo(MARGIN, y);
            cs.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
            cs.stroke();
            y -= 10;
        }

        void writeLine(String text, PDType1Font font, float size, float[] colWidths) throws Exception {
            ensureSpace();
            float x = MARGIN;
            float usableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, y);
            cs.showText(truncate(text, font, size, usableWidth));
            cs.endText();
            y -= LINE_HEIGHT;
        }

        void writeRow(String[] cells, PDType1Font font, float size, boolean shaded) throws Exception {
            ensureSpace();
            float usableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
            float colW = usableWidth / cells.length;

            // shade alternating rows
            if (shaded) {
                cs.setNonStrokingColor(0.93f, 0.93f, 0.93f);
                cs.addRect(MARGIN, y - 3, usableWidth, LINE_HEIGHT);
                cs.fill();
                cs.setNonStrokingColor(0f, 0f, 0f);
            }

            float x = MARGIN + 3;
            cs.beginText();
            cs.setFont(font, size);
            for (int i = 0; i < cells.length; i++) {
                cs.newLineAtOffset(i == 0 ? x : colW, 0);
                cs.showText(truncate(cells[i], font, size, colW - 6));
            }
            cs.endText();
            y -= LINE_HEIGHT;
        }

        void newLine() {
            y -= LINE_HEIGHT / 2;
        }

        void finish() throws Exception {
            if (cs != null) cs.close();
        }

        private void ensureSpace() throws Exception {
            if (y < MARGIN + 30) newPage();
        }

        private String truncate(String text, PDType1Font font, float size, float maxWidth) {
            if (text == null || text.isEmpty()) return "";
            try {
                String safe = text.replaceAll("[^\\x20-\\x7E]", "?");
                while (safe.length() > 1
                        && font.getStringWidth(safe) / 1000f * size > maxWidth) {
                    safe = safe.substring(0, safe.length() - 1);
                }
                return safe;
            } catch (Exception e) {
                return text.replaceAll("[^\\x20-\\x7E]", "?");
            }
        }
    }
}
