package general;

import java.util.Random;

public enum ActionName {
        RIGHT(1, "Right", "R", "\u2192", AxisName.X, 1, 270),
        LEFT(2, "Left", "L", "\u2190", AxisName.X, -1, 90),
        UP(3, "Up", "U", "\u2193", AxisName.Y, 1, 180),
        DOWN(4, "Down", "D", "\u2191",  AxisName.Y, -1, 0),
        THE_END(5, "The End", "E", "",  AxisName.X, 0, -1),
        NONE(6, "None", "NA", "",  AxisName.X, 0, -2);

        private final Integer id;
        private final String text;
        private final String letter;
        private final String arrow;
        private final AxisName axisName;
        private final Integer moveInAxis;
        private final Integer imageRotation;
        /**
         * @param text
         */
        private ActionName(final Integer id, final String text, final String letter, final String arrow,
                           final AxisName axisName, final Integer moveInAxis, final Integer imageRotation) {
            this.id= id;
            this.text = text;
            this.letter = letter;
            this.arrow =arrow;
            this.axisName = axisName;
            this.moveInAxis = moveInAxis;
            this.imageRotation = imageRotation;
        }

        public static Integer getCountOfActions(){
            return 4;
        }

        public static ActionName getById(Integer id){
            switch (id) {
                case 3:
                    return ActionName.UP;
                case 2:
                    return ActionName.DOWN;
                case 0:
                    return ActionName.RIGHT;
                case 1:
                    return ActionName.LEFT;
            }
            return null;
        }

    public static ActionName getRandomAction() {
        Random r = new Random();
        return ActionName.getById(r.nextInt(ActionName.getCountOfActions()));
    }

    @Override
    public String toString() {
            return text;
        }

    public Integer getId() {
            return id;
        }

    public String getLetter() {
            return letter; }

    public String getArrow() {
            return arrow;
        }

    public Integer getImageRotation() {
        return imageRotation;
    }

    public Integer getMoveInAxis() {
        return moveInAxis;
    }

    public AxisName getAxisName() {
        return axisName;
    }
}
