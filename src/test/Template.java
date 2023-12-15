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

        if(templatePath == null)
            throw new IllegalArgumentException("The path must have a valid value.");

        try {
            this.htmlDoc = loadHTMLTemplate(templatePath);
        } catch (IOException e) {
            throw new IOException("The template file could not be found.");
        }
    }

    private Document loadHTMLTemplate(String templatePath) throws IOException {

        Document doc = Jsoup.parse(new File(templatePath), "UTF-8");
        return doc;
    }

    @Override
    public ArrayList<String> format(List<SheetCell> sheetCells) {

        var report = new ArrayList<String>();

        var tableMap = super.groupByLine(sheetCells);

        for (var key : tableMap.keySet()
        ) {
            var line = "";
            var rows = tableMap.get(key);

            line += formatRowId(key, "lineId");

            for (var row: rows
                 ) {
                try {
                    line += formatRow(row);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            // append line to container
            appendRowToElement("lineContainer", key, line);

            report.add(line);
        }
        removeTemplateNodes();
        return  report;
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

    public String formatRowId(int id, String fieldName){

        Element element = htmlDoc.getElementById("lineId");
        element =  element.clone();

        if (element != null && element.children() != null){

            Element finalElment = element;
            element.childNodes().forEach(c -> {

                if(c == null)
                    return;

                if(c.toString().contains("{{"+ fieldName +"}}")){
                    updateNodeText(((Element) c), fieldName, String.valueOf(id));
                }
            });
        }

        return element.html();
    }

    public String formatRow(SheetCell row) throws IllegalAccessException {

        var fields = getTemplateFields();
        Element element = htmlDoc.getElementById("column");;

        element =  element.clone();

        for (var field : fields
             ) {

            field.setAccessible(true);
            Object value = field.get(row);

            if (element != null && element.children() != null){

                Element finalElment = element;
                element.childNodes().forEach(c -> {

                    if(c == null)
                        return;

                    if(c.toString().contains("{{"+ field.getName() +"}}")){
                        updateNodeText(((Element) c), field.getName(), value.toString());
                    }
                });
            }
        }

        return element.html();
    }

    private ArrayList<Field> getTemplateFields() {
        var fields = SheetCell.class.getDeclaredFields();
        ArrayList<Field> fieldsFound = new ArrayList<>();

        for (var field : fields
        ) {
            if(htmlDoc.text().contains(field.getName()))
                fieldsFound.add(field);
        }
        return fieldsFound;
    }

    private void updateNodeText(Element element, String oldText, String newtext){

        var node = element.textNodes().stream().filter(c -> c.toString().contains("{{"+ oldText +"}}")).findFirst();

        if(node.isPresent()){
            String updatedText = node.get().text().replace("{{" + oldText + "}}", newtext);
            node.get().text(updatedText);
        }
    }
}
