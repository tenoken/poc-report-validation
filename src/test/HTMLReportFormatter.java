package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HTMLReportFormatter extends ReportBase implements IReportFormatter {
    private Document htmlDoc;

    public HTMLReportFormatter(String templatePath) {
        try {
            this.htmlDoc = loadHTMLTemplate(templatePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        return null;
    }

    private String formatRow(List<SheetCell> rows) {
        Element content = this.htmlDoc.getElementById("table");
        Element rowContent = this.htmlDoc.getElementById("lineContent");
        Element rowContainer = this.htmlDoc.getElementById("row-{{lineId}}").clone();


        Elements e = content.getElementById("lineId").children().clone();

        for (Element el : e){
            if(el.text().contains("{{lineId}}")){
                var result = el.text().replace("{{lineId}}", String.valueOf(rows.get(0).getLineId()));
                var result2 = rowContainer.id().replace("{{lineId}}", String.valueOf(rows.get(0).getLineId()));
                el.text(result);
                rowContainer.id(result2);
            }
            rowContainer.appendChild(el);
        }

        for (var row: rows
             ) {

            Elements elements = content.getElementById("column").children().clone();
            for (Element element : elements) {

                if(element.text().contains("{{columnName}}")){
                    var result = element.textNodes().get(0).text().replace("{{columnName}}",row.getColumnName());
                    element.text(result);
                }

                if(element.text().contains("{{value}}")){
                    var result = element.text().replace("{{value}}", row.getValue());
                    element.text(result);
                }

            }

            rowContainer.appendChildren(elements.clone());
            rowContent.appendChild(rowContainer);
        }

        return  "";
    }
}
