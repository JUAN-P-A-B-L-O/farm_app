package com.jpsoftware.farmapp.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight animal data embedded in API responses.")
public class AnimalSummaryResponse {

    @Schema(description = "Animal identifier.", example = "animal-001")
    private String id;

    @Schema(description = "Animal tag.", example = "COW-001")
    private String tag;

    public AnimalSummaryResponse() {
    }

    public AnimalSummaryResponse(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
