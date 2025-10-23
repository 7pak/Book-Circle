package com.at.bookcircle.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {
    @Query("""
            SELECT history FROM BookTransactionHistory history
            WHERE history.user.id = :userId
            """)
    Page<BookTransactionHistory> findAllBorrowedBooks(Pageable pageable, Integer userId);

    @Query("""
            SELECT history FROM BookTransactionHistory history
            WHERE history.book.owner.id = :userId
            
            """)
    Page<BookTransactionHistory> findAllReturnedBooks(Pageable pageable, Integer userId);

    @Query("""
            SELECT (COUNT(*) >0) AS isBorrowed
            FROM BookTransactionHistory book
            WHERE book.user.id = :userId AND 
            book.book.id = :bookId AND 
                        book.returnApproved = false 
                       
            """)
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);

    @Query("""
SELECT transaction FROM BookTransactionHistory transaction
WHERE transaction.user.id = :userId AND transaction.book.id =:bookId 
AND transaction.returned = false 
AND transaction.returnApproved = false 
""")
    Optional<BookTransactionHistory> findByUserIdAndBookId(Integer bookId, Integer userId);


    @Query("""
SELECT transaction FROM BookTransactionHistory transaction
WHERE transaction.book.owner.id = :ownerId AND transaction.book.id =:bookId 
AND transaction.returned = true 
AND transaction.returnApproved = false 
""")
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer ownerId);
}