package general.useful;

public enum PropertyTypeEnum {

    NUMBER_OF_ITERATIONS_BEFORE_PI(1, "Number of iterations of ADP before running Policy Iteration."),
    WORLD_HEIGHT(2, "World Height."),
    WORLD_WIDTH(3, "Number of iterations of ADP before running Policy Iteration."),
    NUMBER_OF_ITERATIONS_FOR_SAME_WORLD(4, "Number of iterations using the same world."),
    OBJECT_ANALOGY(5, "Number of object to world size."),
    NUMBER_OF_PARALLEL_AGENTS(6, "Number of agents running at the same time, parallel to each other."),
    END_STATE_X(7, "The place in the x axis for the end state."),
    END_STATE_Y(8, "The place in the x axis for the end state."),
    POLICY_ACCURACY(8, "Number of policies to compare and find equal."),
    REPORTING_ON(8, "Number of policies to compare and find equal."),
    REPORTING_PATH_FOR_FILES(9, "Path to save generated files.");

    private final Integer id;
    private final String description;

    PropertyTypeEnum(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}
