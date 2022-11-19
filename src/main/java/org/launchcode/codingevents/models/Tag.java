package org.launchcode.codingevents.models;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Chris Bay
 */
@Entity
public class Tag extends AbstractEntity {

    @Size(min = 1, max = 25)
    @NotBlank
    private String name;

    @ManyToMany(mappedBy = "tags")
    private final Set<Event> events = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }

    public Tag() {}

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return "#" + name + " ";
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Event> getEvents() {
        return events;
    }
}
