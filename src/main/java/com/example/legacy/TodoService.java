package com.example.legacy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Todo> findByCompleted(boolean completed) {
        return repository.findByCompleted(completed);
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        Todo todo = repository.findById(id).orElse(null);
        if (todo == null) {
            throw new TodoNotFoundException(id);
        }
        return todo;
    }

    public Todo create(Todo todo) {
        todo.setId(null);
        todo.setTitle(normalizeTitle(todo.getTitle()));
        return repository.save(todo);
    }

    public Todo update(Long id, Todo updated) {
        Todo existing = findById(id);
        existing.setTitle(normalizeTitle(updated.getTitle()));
        existing.setCompleted(updated.isCompleted());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Todo existing = findById(id);
        repository.delete(existing);
    }

    private String normalizeTitle(String title) {
        return title == null ? null : title.trim();
    }
}
