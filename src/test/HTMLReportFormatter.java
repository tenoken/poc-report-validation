package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HTMLReportFormatter extends ReportBase implements IReportFormatter {
    private Document htmlDoc;

    public HTMLReportFormatter(String templatePath) throws IOException {

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
            var row = tableMap.get(key);
            report.add(this.formatRow(row));

        }

        // return formattedContent;
        // Remove template nodes
        htmlDoc.getElementById("lineId").remove();
        htmlDoc.getElementById("column").remove();
        htmlDoc.getElementById("row-{{lineId}}").remove();
        return report;
    }

    private String formatRow(List<SheetCell> rows) {

        // TODO: add line id into final report
        // Get fields
        var fields = SheetCell.class.getDeclaredFields();

        // Compare if each field exists within the template
        // and if it does, change the value.
        // Do it only once
        for (var field : fields
             ) {
            if(htmlDoc.text().contains(field.getName()))
                System.out.println("The field " + field.getName() + " has found in the template.");
        }

        // Get document nodes
        // Doc html = ...
        Element content = this.htmlDoc.getElementById("table");
        Element rowContent = this.htmlDoc.getElementById("lineContent");
        Element rowContainer = this.htmlDoc.getElementById("row-{{lineId}}").clone();
        Elements es = content.getElementById("lineId").children().clone();

        // Get value of the fields of each row
        boolean indexPrinted = false;
        for (var sheetCell : rows
             ) {
           var x = sheetCell.getClass().getDeclaredFields();
            Elements elements = content.getElementById("column").children().clone();;

            for (var field : fields
                 ) {
                field.setAccessible(true);

                try {

                    Object value = field.get(sheetCell);

                    // TODO: Distinct value. Change the logic
                    if (indexPrinted && field.getName() == "lineId")
                        continue;

                    System.out.println("The value of this field is: " + value.toString());
                    // Replace value of template


                    for (var element : elements
                         ) {

                        if(element.textNodes().stream().count() > 0){

                            element.textNodes().stream()
                                    .filter(textNode -> textNode.text().contains("{{" + field.getName() + "}}"))
                                    .findFirst()
                                    .ifPresent(textNode -> {
                                        String updatedText = textNode.text().replace("{{" + field.getName() + "}}", value.toString());
                                        textNode.text(updatedText);
                                        element.text(textNode.text());
                                    });
                        }
                    }

                    // TODO: Distinct value. Change the logic
                    if(field.getName() == "lineId"){

                        var result = rowContainer.id().replace("{{" + field.getName() + "}}",  value.toString());
                        rowContainer.id(result);
                        result = rowContainer.text().replace("{{" + field.getName() + "}}",  value.toString());
                        rowContainer.text(result);

                        var node = es.textNodes().stream()
                                .filter(textNode -> textNode.text().contains("{{" + field.getName() + "}}"))
                                .findFirst();
//                                .ifPresent(textNode -> {
//                                    String updatedText = textNode.text().replace("{{" + field.getName() + "}}", value.toString());
//                                    textNode.text(updatedText);
//                                    //e.text(textNode.text());
//                                });
                        if(node.get() != null){
                            String updatedText = node.get().text().replace("{{" + field.getName() + "}}", value.toString());
                            node.get().text(updatedText);
                            rowContainer.appendChildren(es);
                            //e.text(textNode.text());
                        }


                        indexPrinted = true;
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            rowContainer.appendChildren(elements.clone());
            rowContent.appendChild(rowContainer);
        }

        return  rowContainer.html();
    }
}
