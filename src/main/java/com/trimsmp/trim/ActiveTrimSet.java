package com.trimsmp.trim;

/**
 * The trim power a player currently has active, computed from their equipped armor.
 */
public record ActiveTrimSet(TrimPatternKind pattern, TrimTier tier) {
}
