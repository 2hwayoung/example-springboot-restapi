package com.example.restapi;

import com.example.restapi.domain.member.member.entity.Member;
import com.example.restapi.domain.member.member.service.MemberService;
import com.example.restapi.domain.post.post.controller.ApiV1PostController;
import com.example.restapi.domain.post.post.entity.Post;
import com.example.restapi.domain.post.post.service.PostService;
import com.example.restapi.global.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    private MemberService memberService;

    private Member loginMember;
    private String authToken;
    @BeforeEach
    void login() {
        loginMember = memberService.findByUsername("user1").get();
        authToken = memberService.getAuthToken(loginMember);
    }

    private void checkPosts(ResultActions resultActions, List<Post> posts) throws Exception {
        for(int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                .andExpect(jsonPath("$.data.items[%d]".formatted(i)).exists())
                .andExpect(jsonPath("$.data.items[%d].id".formatted(i)).value(post.getId()))
                .andExpect(jsonPath("$.data.items[%d].title".formatted(i)).value(post.getTitle()))
                .andExpect(jsonPath("$.data.items[%d].content".formatted(i)).doesNotExist())
                .andExpect(jsonPath("$.data.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.items[%d].authorName".formatted(i)).value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.items[%d].published".formatted(i)).value(post.isPublished()))
                .andExpect(jsonPath("$.data.items[%d].listed".formatted(i)).value(post.isListed()))
                .andExpect(jsonPath("$.data.items[%d].createdDate".formatted(i)).value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.items[%d].modifiedDate".formatted(i)).value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
        }
    }

    @Test
    @DisplayName("글 다건 조회1")
    void items1() throws Exception {
        int page = 1;
        int pageSize = 3;
        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize))
                .andExpect(jsonPath("$.data.currentPageNo").value(page))
                .andExpect(jsonPath("$.data.totalPages").isNumber());

        Page<Post> postPage = postService.getListedItems(page, pageSize, "", "");
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("글 다건 조회2 - 검색 - 제목")
    void items2() throws Exception {
        int page = 1;
        int pageSize = 3;
        String keywordType = "title";
        String keyword = "title";

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("keywordType", keywordType)
                        .param("keyword", keyword))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize))
                .andExpect(jsonPath("$.data.currentPageNo").value(page))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalItems").value(7));

        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("글 다건 조회3 - 검색 - 내용")
    void items3() throws Exception {
        int page = 1;
        int pageSize = 3;
        String keywordType = "content";
        String keyword = "content";

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts")
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("keywordType", keywordType)
                        .param("keyword", keyword))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize))
                .andExpect(jsonPath("$.data.currentPageNo").value(page))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalItems").value(7));

        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("글 다건 조회 4 - 내가 작성한 글 조회")
    void items4() throws Exception {
        int page = 1;
        int pageSize = 3;
        String keywordType = "";
        String keyword = "";

        ResultActions resultActions = mvc
                .perform(get("/api/v1/posts/mine")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("keywordType", keywordType)
                        .param("keyword", keyword))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getMyItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("내 글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize))
                .andExpect(jsonPath("$.data.currentPageNo").value(page))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.totalItems").value(5));

        Member author = memberService.getMemberByAccessToken(authToken).get();
        Page<Post> postPage = postService.getMyItems(author, page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    private void checkPost(ResultActions resultActions, Post post) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.published").value(post.isPublished()))
                .andExpect(jsonPath("$.data.listed").value(post.isListed()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }

    private ResultActions itemRequest(long postId, String authToken) throws Exception {
        return mvc
                .perform(get("/api/v1/posts/%d".formatted(postId))
                        .header("Authorization", "Bearer " + authToken))
                .andDo(print());
    }

    @Test
    @DisplayName("글 단건 조회 1 - 다른 유저의 공개글 조회")
    void item1() throws Exception {
       long postId = 1;
        ResultActions resultActions = itemRequest(postId, "user2");

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
    @DisplayName("글 단건 조회 2 - 다른 유저의 비공개글 조회")
    void item2() throws Exception {
       long postId = 3;
       Member otherMember = memberService.findByUsername("user2").get();
       String otherToken = memberService.getAuthToken(otherMember);

       ResultActions resultActions = itemRequest(postId, otherToken);

       resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("비공개 설정된 글입니다."));
    }

    @Test
    @DisplayName("글 단건 조회 3 - 존재하지 않는 글 조회")
    void item3() throws Exception {
       long postId = -1;
        ResultActions resultActions = itemRequest(postId, authToken);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    private ResultActions writeRequest(String token, String title, String content) throws Exception {
        return mvc
                .perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer "+token)
                        .content("""
                                {
                                    "title": "%s",
                                    "content": "%s",
                                    "published": true,
                                    "listed": true
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

        ResultActions resultActions = writeRequest(authToken, "새로운 글 제목", "새로운 글 내용");

        Post post = postService.getLatestItem().orElseThrow();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 작성이 완료되었습니다.".formatted(post.getId())));

        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 작성2 - no apiKey")
    void write2() throws Exception {

        ResultActions resultActions = writeRequest("WRONG_ACCESS_TOKEN", "새로운 글 제목", "새로운 글 내용");

        resultActions
                .andExpect(status().isUnauthorized())
//                .andExpect(handler().handlerType(ApiV1PostController.class))
//                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 작성3 - no input data")
    void write3() throws Exception {

        ResultActions resultActions = writeRequest(authToken, "", "");

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));
    }

    private ResultActions modifyRequest(String authToken, long postId, String title, String content) throws Exception {
        return mvc
                .perform(put("/api/v1/posts/%d".formatted(postId))
                        .header("Authorization", "Bearer "+authToken
                        )
                        .content("""
                                {
                                    "title": "%s",
                                    "content": "%s",
                                    "published": true,
                                    "listed": true
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
    @DisplayName("글 수정1")
    void modify1() throws Exception {
        long postId = 1;
        ResultActions resultActions = modifyRequest(authToken, postId, "(모집마감)축구 하실분 모집합니다", "모집이 마감되었습니다.");

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)));

        Post post = postService.getItem(postId).orElseThrow();
        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 수정2 - no apiKey")
    void modify2() throws Exception {
        long postId = 1;
        ResultActions resultActions = modifyRequest("WRONG_ACCESS_TOKEN", postId, "(모집마감)축구 하실분 모집합니다", "모집이 마감되었습니다.");

        resultActions
                .andExpect(status().isUnauthorized())
//                .andExpect(handler().handlerType(ApiV1PostController.class))
//                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 수정3 - no input data")
    void modify3() throws Exception {
        long postId = 1;
        ResultActions resultActions = modifyRequest(authToken, postId, "", "");

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));
    }

    @Test
    @DisplayName("글 수정4 - no permission")
    void modify4() throws Exception {
        long postId = 6;
        ResultActions resultActions = modifyRequest(authToken, postId, "(모집마감)축구 하실분 모집합니다", "모집이 마감되었습니다.");

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."));
    }


    private ResultActions deleteRequest(String authToken, long postId) throws Exception {
        return mvc
                .perform(delete("/api/v1/posts/%d".formatted(postId))
                        .header("Authorization", "Bearer "+authToken))
                .andDo(print());
    }

    @Test
    @DisplayName("글 삭제1")
    void delete1() throws Exception {
        long postId = 1;
        ResultActions resultActions = deleteRequest(authToken, postId);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 삭제가 완료되었습니다.".formatted(postId)));
    }
    @Test
    @DisplayName("글 삭제2 - no apiKey")
    void delete2() throws Exception {
        long postId = 1;
        ResultActions resultActions = deleteRequest("WRONG_ACCESS_TOKEN", postId);
        resultActions
                .andExpect(status().isUnauthorized())
//                .andExpect(handler().handlerType(ApiV1PostController.class))
//                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 삭제3 - no permission")
    void delete3() throws Exception {
        long postId = 6;
        ResultActions resultActions = deleteRequest(authToken, postId);
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."));
    }

    @Test
    @DisplayName("글 삭제4 - admin")
    void delete4() throws Exception {
        long postId = 1;
        Member admin = memberService.findByUsername("admin").get();
        String adminToken = memberService.getAuthToken(admin);

        ResultActions resultActions = deleteRequest(adminToken, postId);
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 삭제가 완료되었습니다.".formatted(postId)));
    }


}
