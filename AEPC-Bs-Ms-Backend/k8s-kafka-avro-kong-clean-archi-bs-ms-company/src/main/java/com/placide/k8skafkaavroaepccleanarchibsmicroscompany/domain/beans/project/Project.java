package com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.beans.project;

public class Project {
    private String projectId;
    private String name;
    private String description;
    private int priority;
    private String state;
    private String createdDate;
    private String companyId;

    public Project() {
    }

    public Project(String projectId, String name, String description, int priority, String state, String createdDate, String companyId) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.state = state;
        this.createdDate = createdDate;
        this.companyId = companyId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return "Project-API[" +
                "project-Id:'" + projectId + '\'' +
                ", project-name:'" + name + '\'' +
                ", description:'" + description + '\'' +
                ", priority=" + priority +
                ", project-state:'" + state + '\'' +
                ", created-date:'" + createdDate + '\'' +
                ']';
    }
}
