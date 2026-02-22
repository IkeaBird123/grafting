package me.IkeaBird132.grafting.manager;

public enum GraftMode {
    NONE,
    LIFE_LINK,
    LOCATION_GRAFT,
    // Sub-states for Location Graft stages
    LOCATION_ENTITY_TELEPORT  // Stage E: clicking entities to teleport them
}