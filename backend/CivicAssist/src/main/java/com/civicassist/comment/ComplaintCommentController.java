package com.civicassist.comment;

import com.civicassist.comment.dto.CommentResponse;
import com.civicassist.comment.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComplaintCommentController {

    private final ComplaintCommentService commentService;

    public ComplaintCommentController(
            ComplaintCommentService commentService
    ) {
        this.commentService = commentService;
    }

    // Citizens can comment only on their own complaints.
    @PostMapping("/complaints/{id}/comments")
    public CommentResponse createCitizenComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return commentService.createCitizenComment(
                id,
                authentication.getName(),
                request
        );
    }

    // Citizens can view comments only on their own complaints.
    @GetMapping("/complaints/{id}/comments")
    public List<CommentResponse> getCitizenComments(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return commentService.getCitizenComments(
                id,
                authentication.getName()
        );
    }

    // Only admins can comment on any complaint.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/complaints/{id}/comments")
    public CommentResponse createAdminComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return commentService.createAdminComment(
                id,
                authentication.getName(),
                request
        );
    }

    // Only admins can view comments on any complaint.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/complaints/{id}/comments")
    public List<CommentResponse> getAdminComments(
            @PathVariable Long id
    ) {
        return commentService.getAdminComments(id);
    }
}