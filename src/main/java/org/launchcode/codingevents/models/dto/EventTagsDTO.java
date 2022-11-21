package org.launchcode.codingevents.models.dto;

import org.launchcode.codingevents.models.Event;
import org.launchcode.codingevents.models.Tag;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class EventTagsDTO {

    @NotNull
    private Event event;

    @NotNull
    private Set<Tag> tags = new HashSet<>();

    public EventTagsDTO() {}

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
