package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HTMLReportFormatter extends ReportBase implements IReportFormatter {
    private Document htmlDoc;
    private Element rowContainer;
    private Element rowContent;
    private Element content;
    private Elements lineIdElements;

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

        // Remove template nodes
        htmlDoc.getElementById("lineId").remove();
        htmlDoc.getElementById("column").remove();
        htmlDoc.getElementById("row-{{lineId}}").remove();
        return report;
    }

    private void removeTemplateNodes(ArrayList<Field> fields) {

        for (var field : fields
             ) {
            var node = htmlDoc.getElementsContainingText(field.getName());
            node.remove();
        }
    }

    private void loadTemplateNodes(){

        content = this.htmlDoc.getElementById("main");
        rowContent = this.htmlDoc.getElementById("lineContent");
        rowContainer = this.htmlDoc.getElementById("row-{{lineId}}").clone();

        var lineId = content.getElementById("lineId");
        if(lineId != null)
            lineIdElements = lineId.children().clone();
    }

    private String formatRow(List<SheetCell> rows) {

        var fields = getTemplateFields();

        // Get document nodes
        loadTemplateNodes();

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

                    for (var element : elements
                         ) {

                        if(element.textNodes().stream().count() > 0){
                            var node = getNodeByText(element, field.getName());

                            if(node.isPresent())
                                updateNodeText(node.get(),field.getName(),value.toString());
                        }
                    }

                    // TODO: Distinct value. Change the logic
                    if(field.getName() == "lineId"){
                        replaceNodeValue(lineIdElements, field, value);
                        indexPrinted = true;
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            rowContainer.appendChildren(elements.clone());
            rowContent.appendChild(rowContainer);
        }

        //removeTemplateNodes(fields);

        return  rowContainer.html();
    }

    private void replaceNodeValue(Elements elements, Field field, Object value) {

        // Get all elements with id equal to class sheet cell field
        var nodes = htmlDoc.getElementsByAttributeValueContaining("id", field.getName());

        if(nodes != null){
            nodes.forEach((n) -> updateNodeId(n,field.getName(), value.toString()));
        }

        // ***

//        var t = htmlDoc.getElementsContainingText(field.getName());
//        var x = getNodeByText(t, field.getName());
//        var y= updateNodeText(x.get(), field.getName(), value.toString());
        //rowContainer.appendChildren(elements);
        // ***

        var node = getNodeByText(elements, field.getName());
        updateNodeText(node.get(),"{{" + field.getName() + "}}", value.toString());

        node = getNodeByText(elements, field.getName());

        if(node != null){
            updateNodeText(node.get(), field.getName(), value.toString());
            rowContainer.appendChildren(elements);
        }
    }

    /**
     *
     * */
    private void updateNodeId(Element element, String field , String value){
        var result = element.id().replace("{{" + field + "}}",value);
        rowContainer.id(result);
    }

    /**
     * Get a node from an element by its text.
     * */
    private Optional<TextNode> getNodeByText(Elements elements, String text){

        return elements.textNodes().stream()
                .filter(textNode -> textNode.text().contains("{{" + text + "}}"))
                .findFirst();
    }

    /**
     * Get a node from an element by its text.
     * */
    private Optional<TextNode> getNodeByText(Element element, String text) {
        return element.textNodes().stream()
                .filter(textNode -> textNode.text().contains("{{" + text + "}}"))
                .findFirst();
    }

    /**
     *  Update a node replacing the current text for a new one.
     * */
    private TextNode updateNodeText(TextNode node, String oldText, String newtext){

        String updatedText = node.text().replace("{{" + oldText + "}}", newtext);
        node.text(updatedText);
        return node;
    }

    /**
     * Verify each field within the template.
     * */
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
}
