package general.useful;

public enum ReportFileEnum {

    UTILITIES(1, "Utilities in each iteration.", "Utilities.txt"),
    POLICIES(2, "Resulting policies after each PI-ADP loop.", "Policies.pdf"),
    GENERAL_REPORT(3, "Resulting policies after each PI-ADP loop.", "GeneralReport.txt"),
    RUN_REPORT(4, "A small PDF showing a summary of the entire process. ADD FILE EXTENSION ", "RunReport");

    private final Integer id;
    private final String description;
    private final String filename;

    ReportFileEnum(Integer id, String description,String filename) {
        this.id = id;
        this.description = description;
        this.filename = filename;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }
}
