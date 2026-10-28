package com.example.demo.controller;

import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> payload) {
        try {
            Long patientId = Long.valueOf(payload.get("patientId").toString());
            Long doctorId = Long.valueOf(payload.get("doctorId").toString());
            Long consultationId = Long.valueOf(payload.get("consultationId").toString());
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = (String) payload.getOrDefault("comment", "");
            Review review = reviewService.createReview(patientId, doctorId, consultationId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Review> getDoctorReviews(@PathVariable Long doctorId) {
        return reviewService.getDoctorReviews(doctorId);
    }

    @GetMapping("/doctor/{doctorId}/average")
    public Map<String, Object> getDoctorAverageRating(@PathVariable Long doctorId) {
        double avg = reviewService.getDoctorAverageRating(doctorId);
        return Map.of("average", avg, "doctorId", doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<Review> getPatientReviews(@PathVariable Long patientId) {
        return reviewService.getPatientReviews(patientId);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkReviewed(@RequestParam Long consultationId) {
        boolean hasReviewed = reviewService.hasReviewedConsultation(consultationId);
        return ResponseEntity.ok(Map.of("hasReviewed", hasReviewed));
    }
}
