package com.civicassist.complaint;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class ComplaintSpecification {

    private ComplaintSpecification() {
    }

    public static Specification<Complaint> filter(
            ComplaintStatus status,
            String category,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Filter by complaint status.
            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            // Filter by category, ignoring letter case.
            if (category != null && !category.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("category")),
                                category.trim().toLowerCase()
                        )
                );
            }

            // Search title and description.
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%"
                        + keyword.trim().toLowerCase()
                        + "%";

                Predicate titleMatches = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        pattern
                );

                Predicate descriptionMatches = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        pattern
                );

                predicates.add(
                        criteriaBuilder.or(
                                titleMatches,
                                descriptionMatches
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}