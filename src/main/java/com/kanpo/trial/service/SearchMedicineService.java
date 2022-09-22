package com.kanpo.trial.service;

import com.kanpo.trial.dao.MedicineDaoImpl;
import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.*;
import com.kanpo.trial.repository.*;
import com.kanpo.trial.restRequest.SendAnswerRequest;
import com.kanpo.trial.restResponse.NextQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 漢方検索サービスクラス
 * @author　keita
 */
@Service
@Transactional
public class SearchMedicineService {
    @PersistenceContext
    EntityManager entityManager;

    MedicineDaoImpl medicineDao;

    /**
     * 漢方名前検索
     *
     * @param searchWord 検索する文字列
     * @param pageSize ページサイズ
     * @param currentPage 取得したいページ番号
     * @return 検索にヒットした漢方一覧
     * @throws Exception 例外
     */
    public List<Medicine> getMatchedSearchWordMedicines(String searchWord,
                                        int pageSize,
                                        int currentPage) throws Exception {
        if (pageSize < 0 || currentPage < 0) {
            throw new Exception("pageSize and currentPage are invalid");
        }
        medicineDao = new MedicineDaoImpl(entityManager);
        List<Medicine> matchList = medicineDao.getMatchedSearchWordMedicines(searchWord, pageSize, currentPage);
        return matchList;
    }
}
