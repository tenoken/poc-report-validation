package test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTests {
    String currentWorkingDirectory;
    String defaultCsvFilePath;

    List<SheetCell> aSet;
    List<SheetCell> bSet;

    @BeforeEach
    void Setup(){
        currentWorkingDirectory = System.getProperty("user.dir");
        defaultCsvFilePath = currentWorkingDirectory + "/src/classic_horror_films.csv";
    }

    // TODO: Test load file by path function

    @Test @Tag("positive")
    void ShouldReturnTrue_WhenReportsAreEqual(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films_2.csv";
        var sut = new ReportValidation();

        // Act
        aSet = sut.loadDataSet(defaultCsvFilePath);
        bSet = sut.loadDataSet(csvFilePath);

        // Assert
        assertTrue(sut.areEqual(aSet, bSet));
    }

    @Test @Tag("negative")
    void ShouldReturnFalse_WhenReportsAreNotEqualLength(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films_2.csv";
        var sut = new ReportValidation();

        // Act
        aSet = sut.loadDataSet(defaultCsvFilePath);
        bSet = sut.loadDataSet(csvFilePath);
        bSet.remove(0);

        // Assert
        assertFalse(sut.areEqual(aSet, bSet));
    }

    @Test @Tag("positive")
    void ShouldReturnTrue_WhenReportsAreEqual_AndOnlyOneIsSorted(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films_2.csv";
        var sut = new ReportValidation();

        // Act
        aSet = sut.loadDataSet(defaultCsvFilePath);
        bSet = sut.loadDataSet(csvFilePath);

        Collections.sort(bSet);

        // Assert
        assertTrue(sut.areEqual(aSet, bSet));
        assertNotEquals(aSet.toString(), bSet.toString());
    }

    @Test @Tag("negative")
    void ShouldReturnTrue_WhenReportsAreEqual_AndNotSorted(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films_2.csv";
        var sut = new ReportValidation();

        // Act
        aSet = sut.loadDataSet(defaultCsvFilePath);
        bSet = sut.loadDataSet(csvFilePath);

        var unsortedSet = bSet.stream().unordered().toList();

        // Assert
        assertTrue(sut.areEqual(aSet, unsortedSet));
        assertEquals(aSet.toString(), bSet.toString());
    }

    @Test @Tag("negative")
    void ShouldReturnException_WhenReportsAreNotEqual(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/classic_horror_films_3.csv";
        var sut = new ReportValidation();

        // Act
        aSet = sut.loadDataSet(defaultCsvFilePath);
        bSet = sut.loadDataSet(csvFilePath);

        // Assert
        assertNotEquals(aSet.toString(), bSet.toString());
        assertThrowsExactly(RuntimeException.class, () -> new ReportValidation().areEqual(aSet, bSet),
                "No matching item found for key: Director and value: Wes Craven");
    }

    @Test @Tag("negative")
    void ShouldReturnException_WhenReportsArgsAreInvalid(){
        // Arrange
        aSet = new ArrayList<>();
        bSet = new ArrayList<>();

        // Act
        var sut = new ReportValidation();

        // Assert
        assertThrowsExactly(IllegalArgumentException.class, () -> sut.areEqual(aSet, bSet),
                "The provided list is empty or null.");
        assertThrowsExactly(IllegalArgumentException.class, () -> sut.areEqual(null, null),
                "The provided list is empty or null.");
    }

    @Test  @Tag("positive")
    void ShouldReturnTheNumberOfLines_WhenGroupedByLine(){
        // Arrange

        // Act
        aSet = new ReportValidation().loadDataSet(defaultCsvFilePath);
        var rows = ReportValidation.groupByLine(aSet);

        // Assert
        assertEquals(rows.size(), ReportValidation.getRowsCount());
    }

    @ParameterizedTest
    @CsvSource({
            "4, The Exorcist",
            "7, Rosemary's Baby",
            "10, A Nightmare on Elm Street",
    })
    void ShouldReturnTrue_WhenQueriedByIndex(int id, String title){
        // Arrange
        aSet = new ReportValidation().loadDataSet(defaultCsvFilePath);

        // Act
        var rows = ReportValidation.groupByLine(aSet);

        // Assert
        assertTrue(rows.get(id).toString().contains(title));
    }

    @Test
    void ShouldReturnTrue_WhenDataSetContainsDuplicateValue(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/shopping.csv";

        // Act
        aSet = new ReportValidation().loadDataSet(csvFilePath);
        bSet = new ReportValidation().loadDataSet(csvFilePath);

        // Assert
        assertTrue(new ReportValidation().areEqual(aSet, bSet));
        assertEquals(aSet.toString(), bSet.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "6, /src/classic_horror_films_2.csv",
            "6, /src/shopping.csv",
    })
    void ShouldReturnFormattedString_WhenReportIsValidCSVFormat(int columnsCount, String path){
        // Arrange
        String csvFilePath = currentWorkingDirectory + path;

        // Act
        aSet = new ReportValidation().loadDataSet(csvFilePath);

        var report = new CSVReportFormatter().format(aSet);
        // Validate report format
        for (var row: report
             ) {
            assertNotNull(row);
            assertEquals(columnsCount, row.split(",").length);
        }
    }

    @Test
    void ShouldThrowException_WhenCSVReportArgsIsInvalid(){
        // Arrange

        // Act

        // Assert
        assertThrowsExactly(IllegalArgumentException.class, () -> new CSVReportFormatter().format(new ArrayList<>()),
                "The provided list is empty or null.");
        assertThrowsExactly(IllegalArgumentException.class, () -> new CSVReportFormatter().format(null),
                "The provided list is empty or null.");
    }

    // TODO: Test HTML format report

    @Test
    void ShouldNotThrowException_WhenSuccessfullyLoadHTMLTemplate(){
        // Arrange
        var path = currentWorkingDirectory + "/src/standard_report.html";

        // Act

        // Assert
        assertDoesNotThrow(() -> new HTMLReportFormatter(path));
        //assertNotNull(() -> new HTMLReportFormatter(path).get);
    }

    @Test
    void ShouldThrowException_WhenLoadHTMLTemplateArgIsInvalid(){
        // Arrange

        // Act

        // Assert
    }

    @Test
    void Should_WhenFormatHTMLReport(){
        // Arrange
        String csvFilePath = currentWorkingDirectory + "/src/shopping.csv";
        var path = currentWorkingDirectory + "/src/standard_report.html";
        //aSet = new ReportValidation().loadDataSet(defaultCsvFilePath);
        aSet = new ReportValidation().loadDataSet(csvFilePath);


        // Act
        var sut = new HTMLReportFormatter(path).format(aSet);

        // Assert
        fail();
    }
}