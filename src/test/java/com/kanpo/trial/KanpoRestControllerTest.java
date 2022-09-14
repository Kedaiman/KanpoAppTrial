package com.kanpo.trial;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.repository.AnalysisRepository;
import com.kanpo.trial.restRequest.SendAnswerRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class KanpoRestControllerTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalysisRepository analysisRepository;

    @BeforeAll
    public static void setup() {
        MyLogger.init();
    }

    @Test
    @DisplayName("/startAnalysis success")
    public void startAnalysisRequest() throws Exception {
        assertEquals(0, analysisRepository.findAll().size());
        mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        assertEquals(1, analysisRepository.findAll().size());
    }

    @Test
    @DisplayName("/sendAnswer success")
    public void sendAnswerRequestExistAnalysisId() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
        int analysisId = ((Integer)map.get("analysisId")).intValue();

        assertEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());

        SendAnswerRequest requestBody = new SendAnswerRequest(Long.valueOf(analysisId), 0);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());
    }

    @Test
    @DisplayName("/sendAnswer fail not exist analysisId")
    public void sendAnswerRequestNotExistAnalysisId() throws Exception {
        SendAnswerRequest requestBody = new SendAnswerRequest(-1, 0);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/sendAnswer fail already end")
    public void sendAnswerRequestAlreadyEnd() throws Exception {
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, 1)");

        SendAnswerRequest requestBody = new SendAnswerRequest(1, 0);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/sendAnswer fail illegal answer")
    public void sendAnswerRequestIllegalAnswer() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
        int analysisId = ((Integer)map.get("analysisId")).intValue();
        int optionLength = ((List)map.get("optionList")).size();

        assertEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());

        SendAnswerRequest requestBody = new SendAnswerRequest(Long.valueOf(analysisId), optionLength);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        requestBody = new SendAnswerRequest(Long.valueOf(analysisId), -1);
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion success")
    public void backQuestionRequest() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
        int analysisId = ((Integer)map.get("analysisId")).intValue();

        assertEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());

        SendAnswerRequest requestBody = new SendAnswerRequest(Long.valueOf(analysisId), 1);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());

        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/" + ((Integer)analysisId).toString()))
                .andExpect(status().isOk());

        assertEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());
    }

    @Test
    @DisplayName("/backQuestion fail not exist analysisId")
    public void backQuestionRequestNotExistAnalysisId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/-1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion fail already end")
    public void backQuestionRequestAlreadyEnd() throws Exception {
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, 1)");
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion fail top Question")
    public void backQuestionRequestTop() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
        int analysisId = ((Integer)map.get("analysisId")).intValue();

        assertEquals(KanpoRestController.topQuestionId, analysisRepository.findById(Long.valueOf(analysisId))
                .get().getNowQuestion().getId());

        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/" + ((Integer)analysisId).toString()))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult success")
    public void getResultRequest() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> map = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw(e);
        }
        int analysisId = ((Integer)map.get("analysisId")).intValue();
        boolean isNextExist = true;


        SendAnswerRequest requestBody = new SendAnswerRequest(Long.valueOf(analysisId), 1);
        ObjectMapper objectMapper = new ObjectMapper();
        while (isNextExist) {
            jsonString = mockMvc.perform(post("/sendAnswer")
                    .content(objectMapper.writeValueAsString(requestBody))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            try {
                map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                e.printStackTrace();
                throw(e);
            }
            isNextExist = ((Boolean)map.get("isNextExist")).booleanValue();
        }

        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/" + ((Integer)analysisId).toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail not exist analysisId")
    public void getResultRequestNotExistAnalysisId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/-1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail yet continue")
    public void getResultRequestYetContinue() throws Exception {
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 0, 1)");
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail illegal answerId")
    public void getResultRequestIllegalAnswerId() throws Exception {
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, -1)");
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/1"))
                .andExpect(status().is(is(500)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @AfterEach
    public void afterEach() {
        jdbc.execute("DELETE FROM analysis");
    }
}
