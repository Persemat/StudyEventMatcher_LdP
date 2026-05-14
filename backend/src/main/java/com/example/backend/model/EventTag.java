package com.example.backend.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EventTag {

    @Column(name = "macro_tag", nullable = false)
    private String macroTag;

    @Column(name = "micro_tag")
    private String microTag;

    public EventTag() {
    }

    public EventTag(String macroTag, String microTag) {
        this.macroTag = macroTag;
        this.microTag = microTag;
    }

    public String getMacroTag() {
        return macroTag;
    }

    public void setMacroTag(String macroTag) {
        this.macroTag = macroTag;
    }

    public String getMicroTag() {
        return microTag;
    }

    public void setMicroTag(String microTag) {
        this.microTag = microTag;
    }

    public String toDisplayLabel() {
        if (microTag == null || microTag.isBlank()) {
            return macroTag;
        }
        return macroTag + " > " + microTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventTag eventTag)) {
            return false;
        }
        return Objects.equals(macroTag, eventTag.macroTag)
                && Objects.equals(microTag, eventTag.microTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macroTag, microTag);
    }
}
