package com.sourcemeter.analyzer.base.jsontree.logical;

import com.sourcemeter.analyzer.base.jsontree.interfaces.LevelInt;

/**
 * Class for encapsulate three levels which implement LevelInt interface.
 */
public class Levels {
    private LevelInt LevelOne;
    private LevelInt LevelTwo;
    private LevelInt LevelThree;

    public Levels(LevelInt levelOne, LevelInt levelTwo, LevelInt levelThree) {
        LevelOne = levelOne;
        LevelTwo = levelTwo;
        LevelThree = levelThree;
    }

    public LevelInt getLevelOne() {
        return LevelOne;
    }

    public LevelInt getLevelTwo() {
        return LevelTwo;
    }

    public LevelInt getLevelThree() {
        return LevelThree;
    }
}
