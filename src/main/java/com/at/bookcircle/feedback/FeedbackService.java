package com.at.bookcircle.feedback;

import com.at.bookcircle.book.Book;
import com.at.bookcircle.book.BookRepository;
import com.at.bookcircle.book.PageResponse;
import com.at.bookcircle.exception.OperationNotPermittedException;
import com.at.bookcircle.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final BookRepository bookRepository;
    private final FeedbackMapper mapper;
    private final FeedbackRepository feedbackRepository;

    public Feedback saveFeedback(FeedbackRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(request.bookId()).orElseThrow(() -> new EntityNotFoundException("No book with id: " + request.bookId()));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Cannot give a feedback to archived book");
        }


        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give a feedback to your own book");
        }

        Feedback feedback = mapper.toFeedback(request);

        return feedbackRepository.save(feedback);

    }

    public PageResponse<FeedbackResponse> findAllFeedbackByBook(Integer bookId, int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(pageable, bookId);

        List<FeedbackResponse> responses = feedbacks.stream().map(f -> mapper.toFeedbackResponse(f, user.getId())).toList();

        return new PageResponse<>(
                responses,
                feedbacks.getNumber(), feedbacks.getSize(), feedbacks.getTotalPages(), feedbacks.getTotalElements(), feedbacks.isFirst(), feedbacks.isLast()
        );
    }
}
