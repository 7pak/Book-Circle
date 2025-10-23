package com.at.bookcircle.feedback;

import com.at.bookcircle.book.Book;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeedbackMapper {
    public Feedback toFeedback(FeedbackRequest request) {
        return Feedback.builder()
                .comment(request.comment())
                .rating(request.rating())
                .book(Book.builder().id(request.bookId()).build())
                .build();
    }

    public FeedbackResponse toFeedbackResponse(Feedback f, Integer userId) {
        return FeedbackResponse.builder()
                .rate(f.getRating())
                .comment(f.getComment())
                .ownFeedback(Objects.equals(f.getCreatedBy(),userId))
                .build();
    }
}
