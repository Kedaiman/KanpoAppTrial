package com.kanpo.trial.dao;

import java.io.Serializable;
import java.util.List;

public interface MedicineDao <T> extends Serializable {
    public List<T> getMatchedSearchWordMedicines(String searchWord, int pageSize, int currentPage);
}
