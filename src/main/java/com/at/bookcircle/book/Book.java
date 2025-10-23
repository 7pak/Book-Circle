package com.at.bookcircle.book;

import com.at.bookcircle.common.BaseEntity;
import com.at.bookcircle.feedback.Feedback;
import com.at.bookcircle.history.BookTransactionHistory;
import com.at.bookcircle.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book extends BaseEntity {

    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;
    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @Transient
    public double getRate(){
        if (feedbacks == null || feedbacks.isEmpty()){
            return 0.0;
        }
        var rate = this.feedbacks.stream().mapToDouble(Feedback::getRating).average().orElse(0.0);

        return Math.round(rate * 10.0) / 10.0;
    }
}
