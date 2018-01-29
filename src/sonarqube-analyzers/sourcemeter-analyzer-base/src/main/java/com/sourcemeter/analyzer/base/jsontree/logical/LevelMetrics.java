package com.sourcemeter.analyzer.base.jsontree.logical;

import com.sourcemeter.analyzer.base.jsontree.interfaces.MetricsInt;

/**
 * Class for encapsulate three levels of metrics which implement MetricsInt interface.
 */
public class LevelMetrics {
    private MetricsInt LevelOneMetrics;
    private MetricsInt LevelTwoMetrics;
    private MetricsInt LevelThreeMetrics;

    public LevelMetrics(MetricsInt levelOneMetrics, MetricsInt levelTwoMetrics, MetricsInt levelThreeMetrics) {
        LevelOneMetrics = levelOneMetrics;
        LevelTwoMetrics = levelTwoMetrics;
        LevelThreeMetrics = levelThreeMetrics;
    }

    public MetricsInt getLevelOneMetrics() {
        return LevelOneMetrics;
    }

    public MetricsInt getLevelTwoMetrics() {
        return LevelTwoMetrics;
    }

    public MetricsInt getLevelThreeMetrics() {
        return LevelThreeMetrics;
    }
}
