package com.company.watermark.repository;

import com.company.watermark.domain.Journal;

import javax.transaction.Transactional;

@Transactional
public interface JournalRepository extends PublicationRepository<Journal> {
}
