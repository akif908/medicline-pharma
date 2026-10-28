package com.example.demo.service;

import com.example.demo.model.Review;
import java.util.List;
//import java.util.Optional;

public interface ReviewService {
    Review createReview(Long patientId, Long doctorId, Long consultationId, int rating, String comment);
    List<Review> getDoctorReviews(Long doctorId);
    List<Review> getPatientReviews(Long patientId);
    double getDoctorAverageRating(Long doctorId);
    boolean hasReviewedConsultation(Long consultationId);
}
