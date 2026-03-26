package com.jpsoftware.farmapp.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight feed type data embedded in API responses.")
public class FeedTypeSummaryResponse {

    @Schema(description = "Feed type identifier.", example = "feed-type-001")
    private String id;

    @Schema(description = "Feed type name.", example = "Corn Silage")
    private String name;

    public FeedTypeSummaryResponse() {
    }

    public FeedTypeSummaryResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
