package test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportBase {

    protected Map<Integer, List<SheetCell>> groupByLine(List<SheetCell> dataSet){
        var map = dataSet.stream()
                .collect(
                        Collectors.groupingBy(SheetCell::getLineId,
                                Collectors.toList()));
        return map;
    }
}
