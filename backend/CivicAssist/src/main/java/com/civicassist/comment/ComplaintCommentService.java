package com.civicassist.comment;

import com.civicassist.comment.dto.CommentResponse;
import com.civicassist.comment.dto.CreateCommentRequest;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.user.AppUser;

import com.civicassist.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComplaintCommentService {

    private final ComplaintCommentRepository commentRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintCommentService(
            ComplaintCommentRepository commentRepository,
            ComplaintRepository complaintRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse createCitizenComment(
            Long complaintId,
            String email,
            CreateCommentRequest request
    ) {
        // Verify that the complaint belongs to the authenticated citizen.
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        AppUser author = findUserByEmail(email);

        ComplaintComment comment = new ComplaintComment(
                complaint,
                author,
                request.content().trim()
        );

        ComplaintComment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCitizenComments(
            Long complaintId,
            String email
    ) {
        // Citizens can view comments only on their own complaints.
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return commentRepository
                .findAllByComplaint_IdOrderByCreatedAtAsc(complaint.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createAdminComment(
            Long complaintId,
            String email,
            CreateCommentRequest request
    ) {
        // Admins can comment on any existing complaint.
        Complaint complaint = complaintRepository
                .findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        AppUser author = findUserByEmail(email);

        ComplaintComment comment = new ComplaintComment(
                complaint,
                author,
                request.content().trim()
        );

        ComplaintComment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getAdminComments(Long complaintId) {

        // Ensure the complaint exists, even if it has no comments.
        complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return commentRepository
                .findAllByComplaint_IdOrderByCreatedAtAsc(complaintId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AppUser findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found"
                ));
    }

    private CommentResponse toResponse(ComplaintComment comment) {
        AppUser author = comment.getAuthor();

        return new CommentResponse(
                comment.getId(),
                comment.getComplaint().getId(),
                author != null ? author.getId() : null,
                author != null ? author.getName() : null,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}