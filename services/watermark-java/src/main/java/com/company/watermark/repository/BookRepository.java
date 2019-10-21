package com.company.watermark.repository;

import com.company.watermark.domain.Book;

import javax.transaction.Transactional;

@Transactional
public interface BookRepository extends PublicationRepository<Book> {
}
