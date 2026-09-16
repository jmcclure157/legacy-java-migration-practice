package com.example.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@Import(WebConfig.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TodoService service;

    private static Todo todo(Long id, String title, boolean completed) {
        Todo todo = new Todo(title, completed);
        todo.setId(id);
        return todo;
    }

    @Test
    public void listReturnsAllTodos() throws Exception {
        List<Todo> todos = Arrays.asList(todo(1L, "write code", false), todo(2L, "ship it", true));
        given(service.findAll()).willReturn(todos);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("write code"))
                .andExpect(jsonPath("$[1].completed").value(true));
    }

    @Test
    public void getReturnsSingleTodo() throws Exception {
        given(service.findById(1L)).willReturn(todo(1L, "write code", false));

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("write code"));
    }

    @Test
    public void getReturnsNotFoundForMissingTodo() throws Exception {
        willThrow(new TodoNotFoundException(42L)).given(service).findById(42L);

        mockMvc.perform(get("/api/todos/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createReturnsCreatedTodo() throws Exception {
        given(service.create(any(Todo.class))).willReturn(todo(3L, "new task", false));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todo(null, "new task", false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("new task"));
    }

    @Test
    public void createRejectsInvalidTodo() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"completed\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateReturnsUpdatedTodo() throws Exception {
        given(service.update(eq(1L), any(Todo.class))).willReturn(todo(1L, "updated", true));

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todo(null, "updated", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updated"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    public void trailingSlashMatchesCollectionRoute() throws Exception {
        given(service.findAll()).willReturn(Arrays.asList(todo(1L, "write code", false)));

        mockMvc.perform(get("/api/todos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void trailingSlashMatchesItemRoute() throws Exception {
        given(service.findById(1L)).willReturn(todo(1L, "write code", false));

        mockMvc.perform(get("/api/todos/1/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
