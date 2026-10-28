package com.example.demo.service.impl;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;

    @Override
    public Review createReview(Long patientId, Long doctorId, Long consultationId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (reviewRepository.findByConsultationId(consultationId).isPresent()) {
            throw new RuntimeException("You have already reviewed this consultation");
        }
        User patient = userRepository.findById(patientId).orElseThrow();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        Consultation consultation = consultationRepository.findById(consultationId).orElseThrow();

        Review review = new Review();
        review.setPatient(patient);
        review.setDoctor(doctor);
        review.setConsultation(consultation);
        review.setRating(rating);
        review.setComment(comment != null ? comment : "");
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getDoctorReviews(Long doctorId) {
        return reviewRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Review> getPatientReviews(Long patientId) {
        return reviewRepository.findByPatientId(patientId);
    }

    @Override
    public double getDoctorAverageRating(Long doctorId) {
        Double avg = reviewRepository.findAverageRatingByDoctorId(doctorId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public boolean hasReviewedConsultation(Long consultationId) {
        return reviewRepository.findByConsultationId(consultationId).isPresent();
    }
}
