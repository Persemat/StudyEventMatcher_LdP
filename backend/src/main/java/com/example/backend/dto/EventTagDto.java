package com.example.backend.dto;

import com.example.backend.model.EventTag;

public record EventTagDto(
        String macroTag,
        String microTag
) {
    public static EventTagDto from(EventTag tag) {
        return new EventTagDto(tag.getMacroTag(), tag.getMicroTag());
    }
}
