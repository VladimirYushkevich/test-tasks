package com.yushkevich.watermark.java.repository;

import com.yushkevich.watermark.java.domain.Journal;

import javax.transaction.Transactional;

@Transactional
public interface JournalRepository extends PublicationRepository<Journal> {
}
