package visualization;

public interface SortStepListener {
    void onStep(SortStep step);

    boolean isCancelled();
}
