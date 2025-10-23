package com.at.bookcircle.book;

import com.at.bookcircle.exception.OperationNotPermittedException;
import com.at.bookcircle.file.FileStorageService;
import com.at.bookcircle.history.BookTransactionHistory;
import com.at.bookcircle.history.BookTransactionHistoryRepository;
import com.at.bookcircle.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private BookRepository bookRepository;
    private BookMapper mapper;
    private BookTransactionHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;


    public Book save(BookRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = mapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book);
    }

    public BookResponse findBookById(Integer bookId) {
        return bookRepository.findById(bookId).map(bookMapper::toBookResponse).
                orElseThrow(() -> new EntityNotFoundException("Book was not found with the id: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponses = books.stream().map(bookMapper::toBookResponse).toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalPages(),
                books.getTotalElements(),
                books.isFirst(),
                books.isLast()
        );

    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponses = books.stream().map(bookMapper::toBookResponse).toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalPages(),
                books.getTotalElements(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBooksResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookTransactionHistory> borrowedBooks = historyRepository.findAllBorrowedBooks(pageable, user.getId());
        List<BorrowedBooksResponse> borrowedBooksResponse = borrowedBooks.stream().map(mapper::toBorrowedBooksResponse).toList();
        return new PageResponse<>(
                borrowedBooksResponse,
                borrowedBooks.getNumber(),
                borrowedBooks.getSize(),
                borrowedBooks.getTotalPages(),
                borrowedBooks.getTotalElements(),
                borrowedBooks.isFirst(),
                borrowedBooks.isLast()
        );

    }

    public PageResponse<BorrowedBooksResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookTransactionHistory> borrowedBooks = historyRepository.findAllReturnedBooks(pageable, user.getId());
        List<BorrowedBooksResponse> borrowedBooksResponse = borrowedBooks.stream().map(mapper::toBorrowedBooksResponse).toList();
        return new PageResponse<>(
                borrowedBooksResponse,
                borrowedBooks.getNumber(),
                borrowedBooks.getSize(),
                borrowedBooks.getTotalPages(),
                borrowedBooks.getTotalElements(),
                borrowedBooks.isFirst(),
                borrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));

        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others book shareable status");
        }

        book.setShareable(!book.isShareable());

        return bookRepository.save(book).getId();

    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));

        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others book archived status");
        }

        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be borrowed since it is archived or not sharable");
        }

        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }

        final boolean isAlreadyBorrowed = historyRepository.isAlreadyBorrowedByUser(bookId, user.getId());

        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("This book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return historyRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBook(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be returned since it is archived or not sharable");
        }


        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot return your own book");
        }

        BookTransactionHistory transactionHistory = historyRepository.findByUserIdAndBookId(bookId,user.getId()).orElseThrow(
                ()-> new OperationNotPermittedException("You didn't borrow this book")
        );

        transactionHistory.setReturned(true);
        return historyRepository.save(transactionHistory).getId();
    }

    public Integer approveReturnedBook(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Book cannot be approved to return since it is archived or not sharable");
        }


        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot approve the return of your own book");
        }
        BookTransactionHistory transactionHistory = historyRepository.findByBookIdAndOwnerId(bookId,user.getId()).orElseThrow(
                ()-> new OperationNotPermittedException("The book is not returned yet")
        );
        transactionHistory.setReturnApproved(true);
        return historyRepository.save(transactionHistory).getId();
    }

    public void uploadBookCoverPic(MultipartFile file, Authentication connectedUser, Integer bookId) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book with id: " + bookId));
        var bookCover = fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
