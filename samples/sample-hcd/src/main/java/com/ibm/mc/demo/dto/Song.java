package com.ibm.mc.demo.dto;

// --- Records ---
public record Song(String band, String title, String[] lyrics, float[][] vectors) {
}
