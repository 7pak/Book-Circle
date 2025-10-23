package com.at.bookcircle.book;

import com.at.bookcircle.file.FileUtils;
import com.at.bookcircle.history.BookTransactionHistory;
import com.at.bookcircle.history.BookTransactionHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class BookMapper {

    public Book toBook(BookRequest request) {
        return Book.builder().id(request.id()).title(request.title()).authorName(request.authorName())
                .synopsis(request.synopsis()).shareable(request.shareable())
                .archived(false).build();
    }
    public BookResponse toBookResponse(Book book) {
        return BookResponse.builder().id(book.getId()).title(book.getTitle()).authorName(book.getAuthorName())
                .synopsis(book.getSynopsis()).sharable(book.isShareable())
                .archived(book.isArchived()).ownerName(book.getOwner().getFullName())
                .cover(FileUtils.readFileFromLocation(book.getBookCover()))
                .build();
    }


    public BorrowedBooksResponse toBorrowedBooksResponse(BookTransactionHistory history) {
        return BorrowedBooksResponse.builder()
                .id(history.getBook().getId()).title(history.getBook().getTitle()).authorName(history.getBook().getAuthorName())
                .isbn(history.getBook().getIsbn())
                .rate(history.getBook().getRate())
                .returned(history.isReturned())
                .returnApproved(history.isReturnApproved())
                .build();
    }
}
