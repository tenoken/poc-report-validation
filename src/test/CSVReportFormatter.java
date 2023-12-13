package test;

import java.util.ArrayList;
import java.util.List;
public class CSVReportFormatter extends ReportBase implements IReportFormatter {

    @Override
    public ArrayList<String> format(List<SheetCell> sheetCells) {

        if(sheetCells == null || sheetCells.size() == 0)
            throw new IllegalArgumentException("The provided list is empty or null.");

        var report = new ArrayList<String>();

        var tableMap = super.groupByLine(sheetCells);

        for (var key : tableMap.keySet()
             ) {
            var row = tableMap.get(key);
            report.add(this.formatRow(row));
        }

        return report;
    }

    private String formatRow(List<SheetCell> sheetCells){
        String row = "";

        row = "Row: " + sheetCells.stream().findFirst().get().getLineId() + ", ";

        for (var cellIterator = sheetCells.iterator(); sheetCells.iterator().hasNext();) {
            SheetCell cell = cellIterator.next();

            if(!cellIterator.hasNext()){
                row += cell.getColumnName() + ":" + cell.getValue();
                break;
            }

            row += cell.getColumnName() + ":" + cell.getValue() + ", ";
        }

        return row;
    }
}
