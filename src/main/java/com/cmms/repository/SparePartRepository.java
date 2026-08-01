package com.cmms.repository;

import com.cmms.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    Optional<SparePart> findByPartNumber(String partNumber);

    @Query("SELECT s FROM SparePart s WHERE s.quantityInStock <= s.reorderLevel")
    List<SparePart> findLowStockParts();
}
