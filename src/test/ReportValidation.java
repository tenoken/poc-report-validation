package test;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportValidation {

    private static int rowsCount = 0;

    public static int getRowsCount() {
        return rowsCount;
    }

    public List<SheetCell> loadDataSet(String path) {

        List<SheetCell> dataSet = new ArrayList<SheetCell>();

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] headers = reader.readNext(); // Read and discard headers
            int index = 0;
            if (headers != null) {

                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    index++;
                    for (int i = 0; i < headers.length; i++)

                        dataSet.add(new SheetCell(index, headers[i], nextLine[i]));
                }
                rowsCount = index;
            } else
                System.out.println("No data found in the CSV file.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return dataSet;
    }

    public boolean areEqual(List<SheetCell> aSet, List<SheetCell> bSet)
            throws IllegalArgumentException {

        if(aSet == null || bSet == null || aSet.isEmpty() || bSet.isEmpty())
            throw new IllegalArgumentException("The provided list is empty or null.");

        // Check if the size of both sets is the same
        if (aSet.size() != bSet.size())
            return false;

        try {
            validateReport(aSet, bSet);
        } catch (SetComparisonException e) {
            // Insert exception in logger provider
            throw new RuntimeException(e);
        }

        return true;
    }

    private boolean validateReport(List<SheetCell> aSet, List<SheetCell> bSet) throws SetComparisonException {

        var setToBeValidated = new ArrayList<SheetCell>(bSet);

        for (SheetCell entry : aSet) {
            entryValidation(setToBeValidated, entry);
            Optional<SheetCell> matchElement = getSheetCell(setToBeValidated, entry);
            // Remove value from set that was already validated
            setToBeValidated.remove(matchElement.get());
        }
        return true;
    }

    private void entryValidation(ArrayList<SheetCell> setToBeValidated, SheetCell entry) throws SetComparisonException {
        // Use Java streams and lambda expressions to check equality
        if (setToBeValidated.stream().noneMatch(bEntry ->
                entry.getColumnName().equals(bEntry.getColumnName()) &&
                        Objects.equals(entry.getValue(), bEntry.getValue()))) {

                throw new SetComparisonException("No matching item found for key: " + entry.getColumnName() +
                        " and value: " + entry.getValue());
        }
    }

    private Optional<SheetCell> getSheetCell(ArrayList<SheetCell> setToBeValidated, SheetCell entry) {
        Optional<SheetCell> matchElement = setToBeValidated.stream()
            .filter(bEntry ->
                            entry.getColumnName().equals(bEntry.getColumnName()) &&
                                    Objects.equals(entry.getValue(), bEntry.getValue())
            )
            .findFirst();
        return matchElement;
    }

    public static Map<Integer, List<SheetCell>> groupByLine(List<SheetCell> dataSet){
        var map = dataSet.stream()
                .collect(
                        Collectors.groupingBy(SheetCell::getLineId,
                                Collectors.toList()));
        return map;
    }
}
