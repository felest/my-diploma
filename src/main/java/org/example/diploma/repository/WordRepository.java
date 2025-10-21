package org.example.diploma.repository;

import org.example.diploma.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WordRepository extends JpaRepository<Word,Long> {
    List<Word> findByModuleId(Long moduleId);
    void deleteByModuleId(Long moduleId);
}
