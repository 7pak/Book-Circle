package com.at.bookcircle.feedback;

import com.at.bookcircle.book.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback")
public class FeedbackController {

    private final FeedbackService service;


    @PostMapping
    public ResponseEntity<Feedback> saveFeedback(@RequestBody @Valid FeedbackRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(service.saveFeedback(request, connectedUser));
    }

    @GetMapping("/book/{book_id}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbackByBook(@PathVariable("book_id") Integer book_id,
                                                                                @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                                @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                                                Authentication connectedUser) {
        return ResponseEntity.ok(service.findAllFeedbackByBook(book_id, page, size, connectedUser));
    }
}

