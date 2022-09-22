package com.kanpo.trial.dao;

import com.kanpo.trial.model.Medicine;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class MedicineDaoImpl implements MedicineDao<Medicine> {
    private static final long serialVersionUID = 1L;

    private EntityManager entityManager;

    public MedicineDaoImpl() {
        super();
    }

    public MedicineDaoImpl(EntityManager entityManager) {
        this();
        this.entityManager = entityManager;
    }

    /**
     * 漢方名検索
     *
     * @param searchWord 検索する文字列
     * @param pageSize ページサイズ
     * @param currentPage 取得したいページ番号
     * @return 検索にヒットした漢方一覧
     */
    @Override
    public List<Medicine> getMatchedSearchWordMedicines(String searchWord, int pageSize, int currentPage) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Medicine> query = builder.createQuery(Medicine.class);
        Root<Medicine> root = query.from(Medicine.class);
        query.select(root)
                .where(builder.or(
                        builder.like(root.get("name"), "%" +searchWord+ "%"),
                        builder.like(root.get("nameKana"),"%" +searchWord+ "%" )));
        List<Medicine> list = null;
        list = (List<Medicine>) entityManager
                .createQuery(query)
                .setFirstResult(currentPage * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        return list;
    }
}
