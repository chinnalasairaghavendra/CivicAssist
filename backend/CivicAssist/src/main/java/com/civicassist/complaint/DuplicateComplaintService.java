package com.civicassist.complaint;

import com.civicassist.complaint.dto.DuplicateComplaintMatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DuplicateComplaintService {

    private static final List<ComplaintStatus> EXCLUDED_STATUSES =
            List.of(
                    ComplaintStatus.RESOLVED,
                    ComplaintStatus.REJECTED
            );

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were",
            "at", "in", "on", "near", "to", "of", "and",
            "for", "with", "by", "from", "there", "this",
            "that", "it", "has", "have", "been"
    );

    private final ComplaintRepository complaintRepository;

    @Value("${civicassist.duplicates.threshold:60}")
    private int threshold;

    @Value("${civicassist.duplicates.max-candidates:500}")
    private int maxCandidates;

    public DuplicateComplaintService(
            ComplaintRepository complaintRepository
    ) {
        this.complaintRepository = complaintRepository;
    }

    @Transactional(readOnly = true)
    public List<DuplicateComplaintMatch> findPotentialDuplicates(
            String title,
            String description,
            String category,
            String address,
            Double latitude,
            Double longitude
    ) {
        Page<Complaint> candidates =
                complaintRepository.findAllByStatusNotIn(
                        EXCLUDED_STATUSES,
                        PageRequest.of(
                                0,
                                maxCandidates,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );

        return candidates.getContent()
                .stream()
                .map(complaint -> createMatch(
                        complaint,
                        title,
                        description,
                        category,
                        address,
                        latitude,
                        longitude
                ))
                .filter(match -> match.similarityScore() >= threshold)
                .sorted((a, b) ->
                        Integer.compare(
                                b.similarityScore(),
                                a.similarityScore()
                        )
                )
                .limit(5)
                .toList();
    }

    private DuplicateComplaintMatch createMatch(
            Complaint complaint,
            String newTitle,
            String newDescription,
            String newCategory,
            String newAddress,
            Double newLatitude,
            Double newLongitude
    ) {
        String existingDescription = safe(complaint.getDescription());
        String existingTitle = safe(complaint.getTitle());

        // Include both title and description when comparing issues.
        String newText = newTitle + " " + newDescription;
        String existingText = existingTitle + " " + existingDescription;

        double descriptionScore =
                jaccardSimilarity(newText, existingText);

        boolean categoryAvailable =
                hasText(newCategory) && hasText(complaint.getCategory());

        boolean categoryMatches = categoryAvailable
                && normalize(newCategory)
                .equals(normalize(complaint.getCategory()));

        boolean coordinatesAvailable =
                newLatitude != null
                        && newLongitude != null
                        && complaint.getLatitude() != null
                        && complaint.getLongitude() != null;

        boolean addressesAvailable =
                hasText(newAddress)
                        && hasText(complaint.getAddress());

        boolean locationAvailable =
                coordinatesAvailable || addressesAvailable;

        double locationScore = 0.0;

        if (coordinatesAvailable) {
            double distance = distanceInMeters(
                    newLatitude,
                    newLongitude,
                    complaint.getLatitude(),
                    complaint.getLongitude()
            );

            // Full location score within 100 m, otherwise no points.
            locationScore = distance <= 100 ? 1.0 : 0.0;
        } else if (addressesAvailable) {
            locationScore = normalize(newAddress)
                    .equals(normalize(complaint.getAddress()))
                    ? 1.0 : 0.0;
        }

        double score;

        if (categoryAvailable && locationAvailable) {
            score = descriptionScore * 60
                    + (categoryMatches ? 20 : 0)
                    + locationScore * 20;
        } else if (categoryAvailable) {
            score = descriptionScore * 75
                    + (categoryMatches ? 25 : 0);
        } else if (locationAvailable) {
            score = descriptionScore * 75
                    + locationScore * 25;
        } else {
            score = descriptionScore * 100;
        }

        return new DuplicateComplaintMatch(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getCategory(),
                complaint.getStatus(),
                (int) Math.round(score)
        );
    }

    private double jaccardSimilarity(String first, String second) {
        Set<String> firstWords = tokenize(first);
        Set<String> secondWords = tokenize(second);

        if (firstWords.isEmpty() || secondWords.isEmpty()) {
            return 0.0;
        }

        long intersection = firstWords.stream()
                .filter(secondWords::contains)
                .count();

        Set<String> union = new java.util.HashSet<>(firstWords);
        union.addAll(secondWords);

        return (double) intersection / union.size();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(
                        normalize(text).split("[^\\p{L}\\p{N}]+")
                )
                .filter(word -> !word.isBlank())
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    private String normalize(String text) {
        return safe(text)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private double distanceInMeters(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        double earthRadius = 6_371_000;

        double latitudeDifference =
                Math.toRadians(lat2 - lat1);

        double longitudeDifference =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return earthRadius * c;
    }
}