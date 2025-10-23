package com.at.bookcircle.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
    public static Specification<Book> withOwnerId(Integer ownerId) {
        return (Specification<Book>) (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);

    }
}
