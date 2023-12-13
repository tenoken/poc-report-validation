package test;

import java.util.ArrayList;
import java.util.List;

public interface IReportFormatter {

    ArrayList<String> format(List<SheetCell> sheetCells);
}
