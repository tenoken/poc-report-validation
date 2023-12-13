package test;

class SheetCell implements Comparable<SheetCell> {
    private final int lineId;
    private final String columnName;
    private final String value;

    String getColumnName(){
        return this.columnName;
    }

    String getValue(){
        return this.value;
    }
    int getLineId(){
        return this.lineId;
    }

    public SheetCell(int index, String columnName, String value) {
        this.lineId = index;
        this.columnName = columnName;
        this.value = value;
    }

    public String toString(){
        return ("(" + lineId + ", " + columnName + ", " + value + ")");
    }

    @Override
    public int compareTo(SheetCell o) {
        // First, compare by lineId
        int lineIdComparison = Integer.compare(this.lineId, o.lineId);

        // If lineId is different, return the comparison result
        if (lineIdComparison != 0) {
            return lineIdComparison;
        }

        // If lineId is the same, compare by columnName
        int columnNameComparison = this.columnName.compareTo(o.columnName);

        // If columnName is different, return the comparison result
        if (columnNameComparison != 0) {
            return columnNameComparison;
        }

        // If both lineId and columnName are the same, compare by value
        return this.value.compareTo(o.value);
    }
}
