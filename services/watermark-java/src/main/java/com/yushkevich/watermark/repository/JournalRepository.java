package com.yushkevich.watermark.repository;

import com.yushkevich.watermark.domain.Journal;

import javax.transaction.Transactional;

@Transactional
public interface JournalRepository extends PublicationRepository<Journal> {
}
