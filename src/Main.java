import java.util.HashMap;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
//import test.SetComparisonException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {

        HashMap<String,String> aSet = new HashMap<String, String>();
        HashMap<String,String> bSet = new HashMap<String, String>();

        String currentWorkingDirectory = System.getProperty("user.dir");
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films.csv";
        String csvFilePath2 = currentWorkingDirectory + "/src/classic_horror_films_2.csv";

        // Load data set by csv file
        loadDataSet(csvFilePath, aSet);
        loadDataSet(csvFilePath2, bSet);
//
//        // Compare sets
//        try {
//            // TODO: Remove the key and value from hashmap after validated
//            areSetsEqual(aSet, bSet);
//            System.out.println("Yay! The sets are equal!");
//        } catch (SetComparisonException e) {
//            System.out.println("Item " + e.getMessage() + "not found!");
//        }
//        catch (RuntimeException e){
//            System.out.println("Item " + e.getMessage() + "not found!");
//        }
    }

    private static void loadDataSet(String path, HashMap<String, String> dataSet) {

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] headers = reader.readNext(); // Read and discard headers
            int index = 0;
            if (headers != null) {

                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    index++;
                    for (int i = 0; i < headers.length; i++)
                        dataSet.put(headers[i] + index, nextLine[i]);
                }
            } else
                System.out.println("No data found in the CSV file.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: create unit tests for this function
//    private static boolean areSetsEqual(Map<String, String> aSet, Map<String, String> bSet) throws SetComparisonException{
//        // Check if the size of both sets is the same
//        if (aSet.size() != bSet.size()) {
//            return false;
//        }
//
//        // Use Java streams and lambda expressions to check equality
////        return aSet.entrySet().stream()
////                .allMatch(entry -> Objects.equals(entry.getValue(), bSet.get(entry.getKey())));
//
//        // Use Java streams and lambda expressions to check equality
//        boolean validUnion;
//        aSet.entrySet().stream().forEach(entry -> {
//            if (bSet.entrySet().stream().noneMatch(bEntry ->
//                    entry.getKey().equals(bEntry.getKey()) && Objects.equals(entry.getValue(), bEntry.getValue()))) {
//                try {
//                    throw new SetComparisonException("No matching item found for key: " + entry.getKey() +
//                            " and value: " + entry.getValue());
//                } catch (SetComparisonException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//        return false;
//    }
}

