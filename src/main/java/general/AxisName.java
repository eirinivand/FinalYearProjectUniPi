package general;

public enum AxisName {
    X("X axis", "x"),
    Y("Y axis", "y");

    private final String text;
    private final String letter;
    /**
     * @param text
     */
    private AxisName(final String text, final String letter) {
        this.text = text;
        this.letter = letter;
    }
    @Override
    public String toString() {
        return letter;
    }

    public String getLetter() {
        return letter;
    }

    public static AxisName getOposite(AxisName axisName) {
        return axisName==AxisName.X? AxisName.Y :AxisName.X;
    }
}
