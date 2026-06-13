package pt.kartodromo.desktop.ui.relatorios;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

public class PdfExporter {

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final PDType1Font FONT_NORMAL =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final float MARGIN = 40f;
    private static final float ROW_HEIGHT = 22f;

    private static final DateTimeFormatter FMT_DT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FMT_NOW =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void exportarClientes(File destino) throws Exception {
        List<Cliente> lista = new ClienteDao().findAll();

        String[] cabecalho = {
                "Nome",
                "Email",
                "Data Nascimento",
                "Nivel"
        };

        String[][] linhas =
                lista.stream()
                        .map(c -> new String[]{
                                safe(c.getNome()),
                                safe(c.getEmail()),
                                c.getDataNascimento() != null
                                        ? c.getDataNascimento().toString()
                                        : "",
                                String.valueOf(c.getNivelExperiencia())
                        })
                        .toArray(String[][]::new);

        escreverRelatorio(
                "Relatorio de Clientes",
                cabecalho,
                linhas,
                "Total de clientes: " + lista.size(),
                destino
        );
    }

    public void exportarReservas(File destino) throws Exception {
        List<Reserva> lista = new ReservaDao().findAllWithDetails();

        String[] cabecalho = {
                "Cliente",
                "Kart",
                "Pista",
                "Inicio",
                "Fim",
                "Estado"
        };

        String[][] linhas =
                lista.stream()
                        .map(r -> new String[]{
                                safe(r.getCliente().getNome()),
                                "#" + r.getKart().getNumero(),
                                safe(r.getPistaNome()),
                                r.getDataHoraInicio().format(FMT_DT),
                                r.getDataHoraFim().format(FMT_DT),
                                safe(r.getEstado().name())
                        })
                        .toArray(String[][]::new);

        escreverRelatorio(
                "Relatorio de Reservas",
                cabecalho,
                linhas,
                "Total de reservas: " + lista.size(),
                destino
        );
    }

    public void exportarCorridas(File destino) throws Exception {
        List<Corrida> lista = new CorridaDao().findAll();

        String[] cabecalho = {
                "Data/Hora",
                "Layout",
                "Categoria",
                "Duracao",
                "Vagas",
                "Cliente"
        };

        String[][] linhas =
                lista.stream()
                        .map(c -> new String[]{
                                c.getDataHoraInicio().format(FMT_DT),
                                safe(c.getLayoutNome()),
                                safe(c.getCategoria().getDescricao()),
                                c.getDuracaoMinutos() + " min",
                                String.valueOf(c.getVagasMaximas()),
                                safe(c.getCliente().getNome())
                        })
                        .toArray(String[][]::new);

        escreverRelatorio(
                "Relatorio de Corridas",
                cabecalho,
                linhas,
                "Total de corridas: " + lista.size(),
                destino
        );
    }

    public void exportarReceita(File destino) throws Exception {
        List<Corrida> lista = new CorridaDao().findAll();

        String[] cabecalho = {
                "Data/Hora",
                "Layout",
                "Categoria",
                "Preco",
                "Vagas",
                "Receita"
        };

        BigDecimal totalReceita = BigDecimal.ZERO;
        String[][] linhas = new String[lista.size()][];

        for (int i = 0; i < lista.size(); i++) {
            Corrida c = lista.get(i);

            BigDecimal preco =
                    c.getCategoria().getPrecoBase();

            BigDecimal receita =
                    preco.multiply(BigDecimal.valueOf(c.getVagasMaximas()));

            totalReceita =
                    totalReceita.add(receita);

            linhas[i] =
                    new String[]{
                            c.getDataHoraInicio().format(FMT_DT),
                            safe(c.getLayoutNome()),
                            safe(c.getCategoria().getDescricao()),
                            String.format("%.2f EUR", preco),
                            String.valueOf(c.getVagasMaximas()),
                            String.format("%.2f EUR", receita)
                    };
        }

        escreverRelatorio(
                "Relatorio de Receita",
                cabecalho,
                linhas,
                "Receita total: "
                        + String.format("%.2f EUR", totalReceita),
                destino
        );
    }

    private void escreverRelatorio(
            String titulo,
            String[] cabecalho,
            String[][] linhas,
            String rodape,
            File destino
    ) throws Exception {

        try (PDDocument document = new PDDocument()) {
            PageWriter writer =
                    new PageWriter(document, titulo);

            writer.writeText(
                    "Gerado em: " + LocalDateTime.now().format(FMT_NOW),
                    FONT_NORMAL,
                    9
            );

            writer.addSpace(12);

            writer.writeTableHeader(cabecalho);

            boolean alternate =
                    false;

            for (String[] linha : linhas) {
                writer.writeTableRow(linha, alternate);
                alternate = !alternate;
            }

            writer.addSpace(16);

            if (rodape != null && !rodape.isBlank()) {
                writer.writeText(
                        rodape,
                        FONT_BOLD,
                        11
                );
            }

            writer.close();
            document.save(destino);
        }
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("ç", "c")
                .replace("Ç", "C")
                .replace("ã", "a")
                .replace("Ã", "A")
                .replace("á", "a")
                .replace("Á", "A")
                .replace("à", "a")
                .replace("À", "A")
                .replace("é", "e")
                .replace("É", "E")
                .replace("í", "i")
                .replace("Í", "I")
                .replace("ó", "o")
                .replace("Ó", "O")
                .replace("õ", "o")
                .replace("Õ", "O")
                .replace("ú", "u")
                .replace("Ú", "U")
                .replace("º", "")
                .replace("ª", "")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private static class PageWriter {

        private final PDDocument document;
        private final String title;

        private PDPage page;
        private PDPageContentStream contentStream;

        private float y;

        PageWriter(PDDocument document, String title) throws Exception {
            this.document = document;
            this.title = title;

            newPage();
        }

        private void newPage() throws Exception {
            if (contentStream != null) {
                contentStream.close();
            }

            page =
                    new PDPage(
                            new PDRectangle(
                                    PDRectangle.A4.getHeight(),
                                    PDRectangle.A4.getWidth()
                            )
                    );

            document.addPage(page);

            contentStream =
                    new PDPageContentStream(document, page);

            y =
                    page.getMediaBox().getHeight() - MARGIN;

            drawHeader();
        }

        private void drawHeader() throws Exception {
            writeTextAt(
                    title,
                    FONT_BOLD,
                    17,
                    MARGIN,
                    y
            );

            y -= 24;

            contentStream.setLineWidth(1f);
            contentStream.moveTo(MARGIN, y);
            contentStream.lineTo(
                    page.getMediaBox().getWidth() - MARGIN,
                    y
            );
            contentStream.stroke();

            y -= 18;
        }

        void writeText(
                String text,
                PDType1Font font,
                float fontSize
        ) throws Exception {

            ensureSpace(ROW_HEIGHT);

            writeTextAt(
                    safe(text),
                    font,
                    fontSize,
                    MARGIN,
                    y
            );

            y -= ROW_HEIGHT;
        }

        void writeTableHeader(String[] cells) throws Exception {
            ensureSpace(ROW_HEIGHT);

            float pageWidth =
                    page.getMediaBox().getWidth();

            float tableWidth =
                    pageWidth - (2 * MARGIN);

            contentStream.setNonStrokingColor(198f / 255f, 40f / 255f, 40f / 255f);
            contentStream.addRect(
                    MARGIN,
                    y - 5,
                    tableWidth,
                    ROW_HEIGHT
            );
            contentStream.fill();

            contentStream.setNonStrokingColor(1f, 1f, 1f);

            writeCells(
                    cells,
                    FONT_BOLD,
                    9,
                    true
            );

            contentStream.setNonStrokingColor(0f, 0f, 0f);

            y -= ROW_HEIGHT;
        }

        void writeTableRow(
                String[] cells,
                boolean shaded
        ) throws Exception {

            ensureSpace(ROW_HEIGHT);

            float pageWidth =
                    page.getMediaBox().getWidth();

            float tableWidth =
                    pageWidth - (2 * MARGIN);

            if (shaded) {
                contentStream.setNonStrokingColor(245f / 255f, 245f / 255f, 245f / 255f);
                contentStream.addRect(
                        MARGIN,
                        y - 5,
                        tableWidth,
                        ROW_HEIGHT
                );
                contentStream.fill();
                contentStream.setNonStrokingColor(0f, 0f, 0f);
            }

            writeCells(
                    cells,
                    FONT_NORMAL,
                    8,
                    false
            );

            y -= ROW_HEIGHT;
        }

        private void writeCells(
                String[] cells,
                PDType1Font font,
                float fontSize,
                boolean header
        ) throws Exception {

            float pageWidth =
                    page.getMediaBox().getWidth();

            float tableWidth =
                    pageWidth - (2 * MARGIN);

            float columnWidth =
                    tableWidth / cells.length;

            for (int i = 0; i < cells.length; i++) {
                float x =
                        MARGIN + (i * columnWidth) + 5;

                String text =
                        truncate(
                                safe(cells[i]),
                                font,
                                fontSize,
                                columnWidth - 10
                        );

                writeTextAt(
                        text,
                        font,
                        fontSize,
                        x,
                        y
                );
            }
        }

        void addSpace(float space) throws Exception {
            y -= space;
            ensureSpace(ROW_HEIGHT);
        }

        private void writeTextAt(
                String text,
                PDType1Font font,
                float fontSize,
                float x,
                float yPosition
        ) throws Exception {

            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, yPosition);
            contentStream.showText(safe(text));
            contentStream.endText();
        }

        private void ensureSpace(float needed) throws Exception {
            if (y - needed < MARGIN) {
                newPage();
            }
        }

        private String truncate(
                String text,
                PDType1Font font,
                float fontSize,
                float maxWidth
        ) {

            if (text == null || text.isBlank()) {
                return "";
            }

            String result =
                    safe(text);

            try {
                while (result.length() > 1
                        && font.getStringWidth(result) / 1000f * fontSize > maxWidth) {

                    result =
                            result.substring(0, result.length() - 1);
                }

                if (!result.equals(text) && result.length() > 4) {
                    result =
                            result.substring(0, result.length() - 3)
                                    + "...";
                }

                return result;

            } catch (Exception e) {
                return result;
            }
        }

        void close() throws Exception {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }
}