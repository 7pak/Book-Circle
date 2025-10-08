package com.at.bookcircle.feedback;

import com.at.bookcircle.book.Book;
import com.at.bookcircle.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feedback extends BaseEntity {

    private double rating;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

}
