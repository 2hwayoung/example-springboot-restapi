package com.example.restapi;

import com.example.restapi.domain.post.post.controller.ApiV1PostController;
import com.example.restapi.domain.post.post.entity.Post;
import com.example.restapi.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    private void checkPost(ResultActions resultActions, Post post) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }

    private ResultActions itemRequest(long postId) throws Exception {
        return mvc
                .perform(get("/api/v1/posts/%d".formatted(postId)))
                .andDo(print());
    }

    @Test
    @DisplayName("글 단건 조회 1")
    void item1() throws Exception {
       long postId = 1;
        ResultActions resultActions = itemRequest(postId);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));

        Post post = postService.getItem(postId).get();

        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 단건 조회 2 - 존재하지 않는 글 조회")
    void item2() throws Exception {
       long postId = -1;
        ResultActions resultActions = itemRequest(postId);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    private ResultActions writeRequest(String title, String content) throws Exception {
        return mvc
                .perform(post("/api/v1/posts")
                        .content("""
                                {
                                    "title": "%s",
                                    "content": "%s"
                                }
                                """
                                .formatted(title, content)
                                .stripIndent())
                        .contentType(
                                new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                        ))
                .andDo(print());
    }

    @Test
    @DisplayName("글 작성")
    void write1() throws Exception {

        ResultActions resultActions = writeRequest("새로운 글 제목", "새로운 글 내용");

        Post post = postService.getLatestItem().orElseThrow();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 작성이 완료되었습니다.".formatted(post.getId())));

        checkPost(resultActions, post);
    }
}
