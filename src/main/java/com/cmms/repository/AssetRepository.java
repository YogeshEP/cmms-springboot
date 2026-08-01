package com.cmms.repository;

import com.cmms.entity.Asset;
import com.cmms.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetCode(String assetCode);
    List<Asset> findByStatus(AssetStatus status);
    List<Asset> findByLocationId(Long locationId);
}
