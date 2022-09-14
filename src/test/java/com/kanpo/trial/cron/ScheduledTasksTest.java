package com.kanpo.trial.cron;

import com.kanpo.trial.KanpoAppTrialApplication;
import com.kanpo.trial.configuration.MyProperties;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.Analysis;
import com.kanpo.trial.repository.AnalysisRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes= KanpoAppTrialApplication.class)
public class ScheduledTasksTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Autowired
    private MyProperties myProperties;

    @Autowired
    private AnalysisRepository analysisRepository;

    public String deleteTimeString;

    @BeforeAll
    public static void setup() {
        MyLogger.init();
    }

    @BeforeEach
    public void beforeEach() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, -2 * myProperties.getCronDeleteMinute());
        date = calendar.getTime();

        Timestamp timestamp = new Timestamp(date.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        deleteTimeString = sdf.format(timestamp);
    }

    @Test
    public void deleteAnalysisZero() {
        assertEquals(0, analysisRepository.findAll().size());
        assertEquals(0, scheduledTasks.scheduledDeleteAnalysis());
        assertEquals(0, analysisRepository.findAll().size());
    }

    @Test
    public void deleteAnalysisOne() {
        jdbc.execute("insert into analysis(id, status, update_at, answer_id) values(1, 0, '" + deleteTimeString + "', 1)");
        assertEquals(1, analysisRepository.findAll().size());
        assertEquals(1, scheduledTasks.scheduledDeleteAnalysis());
        assertEquals(0, analysisRepository.findAll().size());
    }

    @Test
    public void deleteAnalysisTwo() {
        jdbc.execute("insert into analysis(id, status, update_at, answer_id) values(1, 0, '" + deleteTimeString + "', 1)");
        jdbc.execute("insert into analysis(id, status, update_at, answer_id) values(2, 0, '" + deleteTimeString + "', 1)");
        assertEquals(2, analysisRepository.findAll().size());
        assertEquals(2, scheduledTasks.scheduledDeleteAnalysis());
        assertEquals(0, analysisRepository.findAll().size());
    }

    @Test
    public void notDeleteAnalysis() {
        jdbc.execute("insert into analysis(id, status, answer_id) values(1, 0, 1)");
        assertEquals(1, analysisRepository.findAll().size());
        assertEquals(0, scheduledTasks.scheduledDeleteAnalysis());
        assertEquals(1, analysisRepository.findAll().size());
    }

    @AfterEach
    public void afterEach() {
        jdbc.execute("DELETE FROM analysis");
    }
}
