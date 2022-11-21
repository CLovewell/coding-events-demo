package org.launchcode.codingevents.controllers;

import org.launchcode.codingevents.data.EventCategoryRepository;
import org.launchcode.codingevents.data.EventRepository;
import org.launchcode.codingevents.data.TagRepository;
import org.launchcode.codingevents.models.Event;
import org.launchcode.codingevents.models.EventCategory;
import org.launchcode.codingevents.models.Tag;
import org.launchcode.codingevents.models.dto.EventTagsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Chris Bay
 */
@Controller
@RequestMapping("events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public String displayEvents(@RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false) Integer tagId, Model model) {

        if (categoryId == null && tagId == null) {
            model.addAttribute("title", "All Events");
            model.addAttribute("events", eventRepository.findAll());
        }
        else if (tagId == null) {
            Optional<EventCategory> result = eventCategoryRepository.findById(categoryId);
            if (result.isEmpty()) {
                model.addAttribute("title", "Invalid Category ID: " + categoryId);
            } else {
                EventCategory category = result.get();
                model.addAttribute("title", "Events in category: " + category.getName());
                model.addAttribute("events", category.getEvents());
            }
        }
        else if (categoryId == null) {
            Optional<Tag> optionalTag = tagRepository.findById(tagId);
            if (optionalTag.isEmpty()) {
                model.addAttribute("title", "Invalid Tag ID: " + categoryId);
            }
            else {
                Tag tag = optionalTag.get();
                model.addAttribute("title", "Events with tag: " + tag.getDisplayName());
                model.addAttribute("events", tag.getEvents());
            }
        }
        else {
            Optional<EventCategory> optionalEventCategory = eventCategoryRepository.findById(categoryId);
            Optional<Tag> optionalTag = tagRepository.findById(tagId);
            if (optionalEventCategory.isEmpty() && optionalTag.isEmpty()) {
                model.addAttribute("title", "Invalid Category ID: " + categoryId);
                model.addAttribute("subtitle","Invalid Tag ID: " + tagId);
            }
            else if (optionalEventCategory.isEmpty()) {
                Tag tag = optionalTag.get();
                model.addAttribute("title", "Invalid Category ID: " + categoryId);
                model.addAttribute("subtitle", "Events with tag: " + tag.getName());
                model.addAttribute("events", tag.getEvents());
            }
            else if (optionalTag.isEmpty()) {
                EventCategory eventCategory = optionalEventCategory.get();
                model.addAttribute("title", "Invalid Tag ID: " + tagId);
                model.addAttribute("subtitle", "Events in category: " + eventCategory.getName());
                model.addAttribute("events", eventCategory.getEvents());
            }
            else {
                Tag tag = optionalTag.get();
                EventCategory eventCategory = optionalEventCategory.get();
                model.addAttribute("title", "Events in category: " + eventCategory.getName());
                model.addAttribute("subtitle", "with tag: " + tag.getName());
                Collection<Event> results = new ArrayList<>();
                Collection<Event> tagResults = tag.getEvents();
                Collection<Event> categoryResults = eventCategory.getEvents();
                for (Event event : tagResults) {
                    if (categoryResults.contains(event)) {
                        results.add(event);
                    }
                }
                model.addAttribute("events", results);
            }
        }

        return "events/index";
    }

    @GetMapping("create")
    public String displayCreateEventForm(Model model) {
        model.addAttribute("title", "Create Event");
        model.addAttribute(new Event());
        model.addAttribute("categories", eventCategoryRepository.findAll());
        return "events/create";
    }

    @PostMapping("create")
    public String processCreateEventForm(@ModelAttribute @Valid Event newEvent,
                                         Errors errors, Model model) {
        if(errors.hasErrors()) {
            model.addAttribute("title", "Create Event");
            return "events/create";
        }

        eventRepository.save(newEvent);
        return "redirect:";
    }

    @GetMapping("delete")
    public String displayDeleteEventForm(Model model) {
        model.addAttribute("title", "Delete Events");
        model.addAttribute("events", eventRepository.findAll());
        return "events/delete";
    }

    @PostMapping("delete")
    public String processDeleteEventsForm(@RequestParam(required = false) int[] eventIds) {

        if (eventIds != null) {
            for (int id : eventIds) {
                eventRepository.deleteById(id);
            }
        }

        return "redirect:";
    }

    @GetMapping("detail")
    public String displayEventDetails(@RequestParam Integer eventId, Model model) {

        Optional<Event> result = eventRepository.findById(eventId);

        if (result.isEmpty()) {
            model.addAttribute("title", "Invalid Event ID: " + eventId);
        } else {
            Event event = result.get();
            model.addAttribute("title", event.getName() + " Details");
            model.addAttribute("event", event);
            model.addAttribute("tags", event.getTags());
        }

        return "events/detail";
    }

    @GetMapping("add-tags")
    public String displayAddTagsForm(@RequestParam Integer eventId, Model model) {

        Optional<Event> result = eventRepository.findById(eventId);
        if (result.isPresent()) {
            Event event = result.get();
            model.addAttribute("title", "Add tags to: " + event.getName());
            model.addAttribute("tags", tagRepository.findAll());
            EventTagsDTO eventTags = new EventTagsDTO();
            eventTags.setEvent(event);
            model.addAttribute("eventTags", eventTags);
        }
        else {
            model.addAttribute("title", "Invalid Event ID: " +
                    eventId);
        }
        return "events/add-tags";
    }

    @PostMapping("add-tags")
    public String processAddTagsForm(@ModelAttribute @Valid EventTagsDTO eventTags,
                                    Errors errors) {
        if (!errors.hasErrors()) {
            Set<Tag> tags = eventTags.getTags();
            Event event = eventTags.getEvent();
            for (Tag tag : tags) {
                event.addTag(tag);
            }
            eventRepository.save(event);
            return "redirect:detail?eventId=" + event.getId();
        }
        else {
            return "redirect:add-tags?eventId=" + eventTags.getEvent().getId();
        }
    }

    @GetMapping("remove-tags")
    public String displayRemoveTagsForm(@RequestParam Integer eventId, Model model) {

        Optional<Event> result = eventRepository.findById(eventId);
        if (result.isPresent()) {
            Event event = result.get();
            model.addAttribute("title", "Remove tags from: " + event.getName());
            model.addAttribute("event", event);
            model.addAttribute("tags", event.getTags());
        }
        else {
            model.addAttribute("title", "Invalid Event ID: " + eventId);
        }
        return "events/remove-tags";
    }

    @PostMapping("remove-tags")
    public String processRemoveTagsForm(@RequestParam(required = false) Integer[] tagIds,
                                        @RequestParam Integer eventId, Model model) {

        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (tagIds != null) {
                for (Integer id : tagIds) {
                    Optional<Tag> optionalTag = tagRepository.findById(id);
                    optionalTag.ifPresent(event::removeTag);
                }
                eventRepository.save(event);
            }
            model.addAttribute("title", event.getName() + " Details");
            model.addAttribute("event", event);
            model.addAttribute("tags", event.getTags());
            return "redirect:detail?eventId=" + event.getId();
        }
        else {
            model.addAttribute("title", "Invalid Event ID: " + eventId);
            return "events/remove-tags";
        }
    }
}
