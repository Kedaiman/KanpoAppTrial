package com.kanpo.trial.service;

import com.kanpo.trial.KanpoAppTrialApplication;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.Medicine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= KanpoAppTrialApplication.class)
@Transactional
@TestPropertySource("/test.properties")
public class SearchMedicineServiceTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SearchMedicineService searchMedicineService;

    @BeforeAll
    public static void setup() {
        MyLogger.init();
    }

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute("insert into medicine(id,name,name_kana) values (100,'漢方1','かんぽういち')");
        jdbc.execute("insert into medicine(id,name,name_kana) values (101,'漢方2','かんぽうに')");
    }

    @Test
    @DisplayName("getting all match medicines")
    public void getMatchSearchWordMedicinesAll() throws Exception {
        List<Medicine> list = searchMedicineService.getMatchedSearchWordMedicines("漢方", 100, 0);
        assertEquals(2, list.size());
        return;
    }

    @Test
    @DisplayName("getting match medicines using kana")
    public void getMatchSearchWordMedicinesKana() throws Exception {
        List<Medicine> list = searchMedicineService.getMatchedSearchWordMedicines("かんぽう", 100, 0);
        assertEquals(2, list.size());
        return;
    }

    @Test
    @DisplayName("paging test")
    public void getMatchSearchWordMedicinesPaging() throws Exception {
        List<Medicine> list = searchMedicineService.getMatchedSearchWordMedicines("漢方", 1, 0);
        assertEquals(1, list.size());
        assertEquals("漢方1", list.get(0).getName());

        list = searchMedicineService.getMatchedSearchWordMedicines("漢方", 1, 1);
        assertEquals(1, list.size());
        assertEquals("漢方2", list.get(0).getName());
        return;
    }

    @Test
    @DisplayName("invalid parameters")
    public void getMatchSearchWordMedicinesInvalidPara() throws Exception {
        assertThrows(Exception.class, () -> {
            searchMedicineService.getMatchedSearchWordMedicines("漢方", -1, 0);
        });

        assertThrows(Exception.class, () -> {
            searchMedicineService.getMatchedSearchWordMedicines("漢方", 1, -1);
        });
    }
}
