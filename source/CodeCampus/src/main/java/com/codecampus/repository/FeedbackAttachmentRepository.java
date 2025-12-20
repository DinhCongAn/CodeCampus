package com.codecampus.repository;

import com.codecampus.entity.FeedbackAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackAttachmentRepository extends JpaRepository<FeedbackAttachment, Integer> {
    // Tìm tất cả file của 1 feedback
    List<FeedbackAttachment> findByFeedbackId(Integer feedbackId);
}