package com.example.coauthoranalysis.model;

import java.util.List;

public class CollaborationNetwork {
    private String centralAuthorId;
    private List<Collaboration> collaborations;
    private List<Author> relatedAuthors;

    // 构造方法
    public CollaborationNetwork() {}

    public CollaborationNetwork(String centralAuthorId,
                                List<Collaboration> collaborations,
                                List<Author> relatedAuthors) {
        this.centralAuthorId = centralAuthorId;
        this.collaborations = collaborations;
        this.relatedAuthors = relatedAuthors;
    }

    // Getter 和 Setter 方法
    public String getCentralAuthorId() {
        return centralAuthorId;
    }

    public void setCentralAuthorId(String centralAuthorId) {
        this.centralAuthorId = centralAuthorId;
    }

    public List<Collaboration> getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(List<Collaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public List<Author> getRelatedAuthors() {
        return relatedAuthors;
    }

    public void setRelatedAuthors(List<Author> relatedAuthors) {
        this.relatedAuthors = relatedAuthors;
    }

    // 添加辅助方法
    public void addCollaboration(Collaboration collaboration) {
        this.collaborations.add(collaboration);
    }

    public void addRelatedAuthor(Author author) {
        this.relatedAuthors.add(author);
    }
}