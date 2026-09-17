package com.example.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TodoServiceTest {

    @Autowired
    private TodoService service;

    @Test
    public void crudRoundTrip() {
        Todo created = service.create(new Todo("  buy milk  ", false));
        assertNotNull(created.getId());
        assertEquals("buy milk", created.getTitle());

        Todo fetched = service.findById(created.getId());
        assertEquals(created.getId(), fetched.getId());

        Todo updated = service.update(created.getId(), new Todo("buy oat milk", true));
        assertEquals("buy oat milk", updated.getTitle());
        assertTrue(updated.isCompleted());

        List<Todo> completed = service.findByCompleted(true);
        assertTrue(completed.stream().anyMatch(t -> t.getId().equals(created.getId())));

        service.delete(created.getId());
        assertThrows(TodoNotFoundException.class, () -> service.findById(created.getId()));
    }

    @Test
    public void findByTitleMatchesPartialAndIgnoresCase() {
        Todo created = service.create(new Todo("Buy Oranges", false));

        List<Todo> matches = service.findByTitle("orange");
        assertTrue(matches.stream().anyMatch(t -> t.getId().equals(created.getId())));

        assertFalse(service.findByTitle("bananas").stream()
                .anyMatch(t -> t.getId().equals(created.getId())));
    }
}
