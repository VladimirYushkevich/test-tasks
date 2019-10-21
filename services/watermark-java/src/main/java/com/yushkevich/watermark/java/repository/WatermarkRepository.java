package com.yushkevich.watermark.java.repository;

import com.yushkevich.watermark.java.domain.Watermark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatermarkRepository extends JpaRepository<Watermark, UUID> {

    Watermark findById(UUID id);
}
