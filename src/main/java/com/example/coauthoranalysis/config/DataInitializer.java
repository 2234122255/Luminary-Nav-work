package com.example.coauthoranalysis.config;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Institution;
//import com.example.coauthoranalysis.repository.AuthorRepository;
//import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.InstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

@Configuration(proxyBeanMethods = false)
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(
            AuthorRepository authorRepository,
            InstitutionRepository institutionRepository,
            AsyncDataInitializer asyncDataInitializer) {
        return args -> {
            logger.info("Starting data initialization process...");

            // 1. 同步执行作者数据初始化
            initAuthorData(authorRepository);

            // 2. 同步执行机构数据初始化（先处理较小的数据集）
            initInstitutionData(authorRepository, institutionRepository);

            // 3. 异步执行合作数据初始化
            logger.info("Starting async collaboration data initialization...");
            asyncDataInitializer.initCollaborationDataAsync();
        };
    }

    @Transactional
    protected void initAuthorData(AuthorRepository authorRepository) {
        try {
            authorRepository.count(); // 触发表创建
            logger.info("AUTHOR table exists, proceeding with data import...");
        } catch (Exception e) {
            logger.error("AUTHOR table does not exist yet", e);
            return;
        }

        if (authorRepository.count() == 0) {
            logger.info("Importing author data...");
            try (InputStream inputStream = new ClassPathResource("filtered_author_nodes_with_scores.csv").getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                reader.readLine(); // Skip header

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    if (parts.length >= 11) {
                        Author author = new Author();
                        author.setId(parts[0].trim());
                        author.setName(parts[1].trim());
                        author.setOrg(parts[2].trim());
                        author.setPaperCount(parseInt(parts[3]));
                        author.setTotalCitations(parseInt(parts[4]));
                        author.setHIndex(parseInt(parts[5]));
                        author.setHarmonicMean(parseDouble(parts[6]));
                        author.setPagerankScore(parseDouble(parts[7]));
                        author.setCollaboratorCount(parseInt(parts[8]));
                        author.setBetweennessCentrality(parseDouble(parts[9]));
                        author.setTotalScore(parseDouble(parts[10]));

                        authorRepository.save(author);
                        count++;

                        if (count % 100 == 0) {
                            logger.info("Imported {} authors...", count);
                        }
                    }
                }
                logger.info("Successfully imported {} authors", count);
            } catch (Exception e) {
                logger.error("Author data import failed", e);
            }
        } else {
            logger.info("Author data already exists, skipping import");
        }
    }

    @Transactional
    protected void initInstitutionData(AuthorRepository authorRepository,
                                       InstitutionRepository institutionRepository) {
        try {
            institutionRepository.count();
            logger.info("INSTITUTION table exists, proceeding with data aggregation...");
        } catch (Exception e) {
            logger.error("INSTITUTION table does not exist yet", e);
            return;
        }

        if (institutionRepository.count() == 0) {
            logger.info("Aggregating institution data...");
            Map<String, Institution> institutionMap = new HashMap<>();

            authorRepository.findAll().forEach(author -> {
                String orgName = author.getOrg();
                if (orgName == null || orgName.trim().isEmpty()) return;

                String processedName = orgName.length() > 1000 ? orgName.substring(0, 1000) : orgName;

                Institution institution = institutionMap.computeIfAbsent(processedName, name -> {
                    Institution inst = new Institution();
                    inst.setName(name);
                    inst.setPaperCount(0);
                    inst.setTotalCitations(0);
                    inst.setAverageScore(0.0);
                    return inst;
                });

                institution.setPaperCount(institution.getPaperCount() + author.getPaperCount());
                institution.setTotalCitations(institution.getTotalCitations() + author.getTotalCitations());
            });

            int[] count = {0};
            institutionMap.forEach((orgName, institution) -> {
                try {
                    Double avgScore = authorRepository.getAverageScoreByOrg(orgName);
                    institution.setAverageScore(avgScore != null ? avgScore : 0.0);
                    institutionRepository.save(institution);

                    if (++count[0] % 100 == 0) {
                        logger.info("Processed {} institutions...", count[0]);
                    }
                } catch (Exception e) {
                    logger.error("Error processing institution: {}", orgName, e);
                }
            });

            logger.info("Successfully processed {} institutions", count[0]);
        } else {
            logger.info("Institution data already exists, skipping aggregation");
        }
    }
}