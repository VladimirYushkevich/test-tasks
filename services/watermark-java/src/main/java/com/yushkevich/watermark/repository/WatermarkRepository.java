package com.yushkevich.watermark.repository;

import com.yushkevich.watermark.domain.Watermark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatermarkRepository extends JpaRepository<Watermark, UUID> {

    Watermark findById(UUID id);
}
