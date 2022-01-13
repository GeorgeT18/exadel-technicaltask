package com.exadel.technicaltask.parser;

enum ParserSeekerStage {
    STARTING,
    LOOKING_FOR_TEXT,
    REACHED_DELIMITER,
    REACHED_NODE_GROUP_START,
    REACHED_NODE_GROUP_END,
    REACHED_END
}
