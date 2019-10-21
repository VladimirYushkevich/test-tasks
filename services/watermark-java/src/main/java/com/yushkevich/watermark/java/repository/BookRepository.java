package com.yushkevich.watermark.java.repository;

import com.yushkevich.watermark.java.domain.Book;

import javax.transaction.Transactional;

@Transactional
public interface BookRepository extends PublicationRepository<Book> {
}
