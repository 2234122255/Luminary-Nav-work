package com.example.coauthoranalysis.config;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Collaboration;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.CollaborationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.Double.parseDouble;

@Service
public class AsyncDataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncDataInitializer.class);

    private final AuthorRepository authorRepository;
    private final CollaborationRepository collaborationRepository;

    public AsyncDataInitializer(AuthorRepository authorRepository,
                                CollaborationRepository collaborationRepository) {
        this.authorRepository = authorRepository;
        this.collaborationRepository = collaborationRepository;
    }

    @Async
    @Transactional
    public Future<String> initCollaborationDataAsync() {
        try {
            collaborationRepository.count();
            logger.info("COLLABORATION table exists, starting async import...");
        } catch (Exception e) {
            logger.error("COLLABORATION table not available", e);
            return CompletableFuture.completedFuture("Failed");
        }

        if (collaborationRepository.count() == 0) {
            try (InputStream inputStream = new ClassPathResource("edges.csv").getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                reader.readLine(); // Skip header
                String line;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    processCollaborationLine(line, count++);
                    if (count % 100 == 0) {
                        logger.info("Async imported {} collaborations...", count);
                    }
                }
                logger.info("Async import completed. Total: {}", count);
                return CompletableFuture.completedFuture("Success");
            } catch (Exception e) {
                logger.error("Async collaboration import failed", e);
                return CompletableFuture.completedFuture("Failed");
            }
        } else {
            logger.info("Collaboration data already exists, skipping async import");
            return CompletableFuture.completedFuture("Skipped");
        }
    }

    private void processCollaborationLine(String line, int count) {
        try {
            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 2) {
                String author1Id = parts[0].trim();
                String author2Id = parts[1].trim();

                Optional<Author> author1 = authorRepository.findById(author1Id);
                Optional<Author> author2 = authorRepository.findById(author2Id);

                if (author1.isPresent() && author2.isPresent()) {
                    Collaboration collaboration = new Collaboration();
                    collaboration.setAuthor1(author1.get());
                    collaboration.setAuthor2(author2.get());
                    collaboration.setWeight(parts.length > 2 ? parseDouble(parts[2]) : 1.0);
                    collaborationRepository.save(collaboration);
                } else {
                    logger.warn("Authors not found for line {}: {} - {}", count, author1Id, author2Id);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing line {}: {}", count, line, e.getMessage());
        }
    }
}