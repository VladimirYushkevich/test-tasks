package com.yushkevich.watermark.repository;

import com.yushkevich.watermark.domain.Book;

import javax.transaction.Transactional;

@Transactional
public interface BookRepository extends PublicationRepository<Book> {
}
