package com.kanpo.trial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.repository.AnalysisRepository;
import com.kanpo.trial.restRequest.SendAnswerRequest;
import com.kanpo.trial.restResponse.NextQuestion;
import com.kanpo.trial.service.QuestionTreeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource("/test.properties")
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
        // Analysisオブジェクトが存在しないことを確認
        assertEquals(0, analysisRepository.findAll().size());
        // API実行し、成功することを確認
        mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // Analysisオブジェクトが1件登録済みであることを確認
        assertEquals(1, analysisRepository.findAll().size());
    }

    @Test
    @DisplayName("/sendAnswer success")
    public void sendAnswerRequestExistAnalysisId() throws Exception {
        // startAnalysis APIでanalysisIdを取得する
        long analysisId = this.executeStartAnalysisFromApi().getAnalysisId();

        // analysisIdに紐づく質問IDはトップの質問であることを確認
        assertEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());

        // sendAnswer APIを実行する
        SendAnswerRequest requestBody = new SendAnswerRequest(analysisId, 0);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // analysisIdに紐づく質問IDがトップ質問以外になっていることを確認
        assertNotEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());
    }

    @Test
    @DisplayName("/sendAnswer fail not exist analysisId")
    public void sendAnswerRequestNotExistAnalysisId() throws Exception {
        // 存在しないanalysisIdでsendAnswer APIを実行
        // 400が返却されることを確認
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
        // Analysisオブジェクトを登録 (status=1[END])
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, 1)");

        // sendAnswer API実行
        // 400が返却されることを確認
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
        // startAnalysis APIを実行
        NextQuestion nextQuestion = this.executeStartAnalysisFromApi();
        long analysisId = nextQuestion.getAnalysisId();
        int optionLength = nextQuestion.getOptionList().size();

        // sendAnswer APIを実行
        // 400が返却されることを確認
        SendAnswerRequest requestBody = new SendAnswerRequest(analysisId, optionLength);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // sendAnswer APIを実行
        // 400が返却されることを確認
        requestBody = new SendAnswerRequest(analysisId, -1);
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion success")
    public void backQuestionRequest() throws Exception {
        // startAnalysis APIを実行
        long analysisId = this.executeStartAnalysisFromApi().getAnalysisId();

        // analysisIdに紐づく質問IDはトップの質問であることを確認
        assertEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());

        // sendAnswer APIを実行
        SendAnswerRequest requestBody = new SendAnswerRequest(analysisId, 1);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/sendAnswer")
                .content(objectMapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // analysisIdに紐づく質問IDがトップ質問以外になっていることを確認
        assertNotEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());

        // backQuestion APIを実行
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/" + ((Long)analysisId).toString()))
                .andExpect(status().isOk());

        // analysisIdに紐づく質問IDはトップの質問であることを確認
        assertEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());
    }

    @Test
    @DisplayName("/backQuestion fail not exist analysisId")
    public void backQuestionRequestNotExistAnalysisId() throws Exception {
        // 存在しないanalysisIdでbackQuestion APIを実行
        // 400が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/-1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion fail already end")
    public void backQuestionRequestAlreadyEnd() throws Exception {
        // Analysisオブジェクトを登録 (status=1[END])
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, 1)");

        // backQuestion API実行
        // 400が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/backQuestion fail top Question")
    public void backQuestionRequestTop() throws Exception {
        // startAnalysis APIを実行
        long analysisId = this.executeStartAnalysisFromApi().getAnalysisId();

        // analysisIdに紐づく質問がトップ質問であることを確認
        assertEquals(QuestionTreeService.topQuestionId, analysisRepository.findById(analysisId)
                .get().getNowQuestion().getId());

        // backQuestion APIを実行
        // 400が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/backQuestion/" + ((Long)analysisId).toString()))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult success")
    public void getResultRequest() throws Exception {
        // startAnalysis APIを実行
        long analysisId = this.executeStartAnalysisFromApi().getAnalysisId();
        boolean isNextExist = true;
        ObjectMapper objectMapper = new ObjectMapper();
        // 質問treeが終了するまでループ
        while (isNextExist) {
            // sendAnswer APIを実行
            isNextExist = this.executeSendAnswerApi(analysisId).getIsNextExist();
        }

        // getResult API実行
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/" + ((Long)analysisId).toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail not exist analysisId")
    public void getResultRequestNotExistAnalysisId() throws Exception {
        // 存在しないanalysisIdでgetResult API実行
        // 400が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/-1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail yet continue")
    public void getResultRequestYetContinue() throws Exception {
        // 継続中のstatusのAnalysisオブジェクトも登録
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 0, 1)");
        // getResult APIを実行
        // 400が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/1"))
                .andExpect(status().is(is(400)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/getResult fail illegal answerId")
    public void getResultRequestIllegalAnswerId() throws Exception {
        // answerId=-1(不正な値)のAnalysisオブジェクトを登録
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 1, -1)");
        // getResult APIを実行
        // 500が返却されることを確認
        mockMvc.perform(MockMvcRequestBuilders
                .get("/getResult/1"))
                .andExpect(status().is(is(500)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("/searchMedicines success")
    public void searchMedicinesSuccess() throws Exception {
        jdbc.execute("insert into medicine(id,name,name_kana) values (100,'漢方1','かんぽういち')");
        jdbc.execute("insert into medicine(id,name,name_kana) values (101,'漢方2','かんぽうに')");

        mockMvc.perform(MockMvcRequestBuilders
                .get("/searchMedicines")
                .param("searchWord", "漢方")
                .param("pageSize", "100")
                .param("currentPage", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("/searchMedicines invalid parameters")
    public void searchMedicinesInvalidParameters() throws Exception {
        jdbc.execute("insert into medicine(id,name,name_kana) values (100,'漢方1','かんぽういち')");
        jdbc.execute("insert into medicine(id,name,name_kana) values (101,'漢方2','かんぽうに')");

        mockMvc.perform(MockMvcRequestBuilders
                .get("/searchMedicines")
                .param("searchWord", "漢方")
                .param("pageSize", "abc")
                .param("currentPage", "0"))
                .andExpect(status().is(is(400)));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/searchMedicines")
                .param("searchWord", "漢方")
                .param("pageSize", "100")
                .param("currentPage", "abc"))
                .andExpect(status().is(is(400)));
    }

    @AfterEach
    public void afterEach() {
        jdbc.execute("DELETE FROM analysis");
    }

    private NextQuestion executeStartAnalysisFromApi() throws Exception {
        String jsonString = mockMvc.perform(MockMvcRequestBuilders.get("/startAnalysis"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        try {
            NextQuestion nextQuestion = mapper.readValue(jsonString, NextQuestion.class);
            return nextQuestion;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private NextQuestion executeSendAnswerApi(long analysisId) throws Exception {
        SendAnswerRequest requestBody = new SendAnswerRequest(analysisId, 1);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mockMvc.perform(post("/sendAnswer")
                .content(mapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        try {
            NextQuestion nextQuestion = mapper.readValue(jsonString, NextQuestion.class);
            return nextQuestion;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
