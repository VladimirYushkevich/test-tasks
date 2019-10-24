package com.yushkevich.watermark.repository;

import com.yushkevich.watermark.domain.Publication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PublicationRepository<T extends Publication> extends JpaRepository<T, Long> {

    T findById(Long id);

    Page<T> findAll(Pageable pageable);
}
