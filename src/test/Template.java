package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Template extends ReportBase implements IReportFormatter {

    private Document htmlDoc;

    public Template(String templatePath) throws IOException {
        if (templatePath == null) {
            throw new IllegalArgumentException("The path must have a valid value.");
        }
        this.htmlDoc = loadHTMLTemplate(templatePath);
    }

    private Document loadHTMLTemplate(String templatePath) throws IOException {
        return Jsoup.parse(new File(templatePath), "UTF-8");
    }

    @Override
    public ArrayList<String> format(List<SheetCell> sheetCells) {
        ArrayList<String>  report = new ArrayList<>();

        var tableMap = super.groupByLine(sheetCells);

        for (var key : tableMap.keySet()) {
            var line = formatLine(tableMap.get(key), key);
            appendRowToElement("lineContainer", key, line);
            report.add(line);
        }

        removeTemplateNodes();
        return report;
    }

    private String formatLine(List<SheetCell> rows, int key) {
        var line = new StringBuilder();
        line.append(formatRowId(key, "lineId"));

        for (var row : rows) {
            try {
                line.append(formatRow(row));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return line.toString();
    }

    private void appendRowToElement(String key, int rowId, String line) {
        var clone = htmlDoc.getElementById(key).clone();
        clone.append(line);
        clone.id(String.valueOf(rowId));
        clone.appendTo(htmlDoc.getElementById("content"));
    }

    private void removeTemplateNodes() {
        htmlDoc.getElementById("lineId").remove();
        htmlDoc.getElementById("column").remove();
        htmlDoc.getElementById("lineContainer").remove();
    }

    public String formatRowId(int id, String fieldName) {
        Element element = htmlDoc.getElementById("lineId").clone();
        updateNodeText(element, fieldName, String.valueOf(id));

        replaceKeyByValue(element, fieldName, String.valueOf(id));

        return element.html();
    }

    public String formatRow(SheetCell row) throws IllegalAccessException {
        var fields = getTemplateFields();
        Element element = htmlDoc.getElementById("column").clone();

        for (var field : fields) {
            field.setAccessible(true);
            Object value = field.get(row);

            replaceKeyByValue(element, field.getName(), value.toString());
        }

        return element.html();
    }

    private void replaceKeyByValue(Element element, String  fieldName, String value) {
        if (element != null && element.children() != null){

            Element finalElment = element;
            element.childNodes().forEach(c -> {

                if(c == null)
                    return;

                if(c.toString().contains("{{"+ fieldName +"}}")){
                    updateNodeText(((Element) c), fieldName, value);
                }
            });
        }
    }

    private ArrayList<Field> getTemplateFields() {
        var fields = SheetCell.class.getDeclaredFields();
        ArrayList<Field> fieldsFound = new ArrayList<>();

        for (var field : fields) {
            if (htmlDoc.text().contains(field.getName())) {
                fieldsFound.add(field);
            }
        }
        return fieldsFound;
    }

    private void updateNodeText(Element element, String oldText, String newtext) {
        var node = element.textNodes().stream()
                .filter(c -> c.toString().contains("{{" + oldText + "}}"))
                .findFirst();

        node.ifPresent(n -> {
            String updatedText = n.text().replace("{{" + oldText + "}}", newtext);
            n.text(updatedText);
        });
    }
}
