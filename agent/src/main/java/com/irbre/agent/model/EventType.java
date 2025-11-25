package com.irbre.agent.model;

/**
 * Represents the type of event captured by the agent.
 */
public enum EventType {
    METHOD_ENTRY,       // A method begins execution
    METHOD_EXIT,        // A method returns normally
    EXCEPTION,    // A method throws an exception
    REQUEST_START,       // Beginning of an incoming HTTP request
    REQUEST_END          // End of an incoming HTTP request
}

