package com.ibm.mc.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Lyric(String band, String title, String lyric, @JsonProperty("$vector") float[] lyricVector) {
}
