package com.sfp.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for parsing, formatting, and manipulating dates in ISO-8601 format.
 */
public class DateUtil {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Returns today's date formatted as YYYY-MM-DD.
     */
    public static String todayIso() {
        return LocalDate.now().format(ISO_FORMATTER);
    }

    /**
     * Returns current calendar year.
     */
    public static int currentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * Formats a LocalDate to standard ISO-8601 YYYY-MM-DD.
     */
    public static String formatIso(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(ISO_FORMATTER);
    }

    /**
     * Formats an ISO-8601 string to a friendly readable display format (e.g., 15 Jan 2026).
     */
    public static String formatDisplay(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.trim().isEmpty()) {
            return "";
        }
        try {
            String datePart = isoDateStr.trim();
            if (datePart.length() >= 10) {
                datePart = datePart.substring(0, 10);
            }
            LocalDate parsed = LocalDate.parse(datePart, ISO_FORMATTER);
            return parsed.format(DISPLAY_FORMATTER);
        } catch (DateTimeParseException e) {
            return isoDateStr;
        }
    }

    /**
     * Parses an ISO-8601 string to LocalDate, or returns null if unparseable.
     */
    public static LocalDate parseIso(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.trim().isEmpty()) {
            return null;
        }
        try {
            String datePart = isoDateStr.trim();
            if (datePart.length() >= 10) {
                datePart = datePart.substring(0, 10);
            }
            return LocalDate.parse(datePart, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
