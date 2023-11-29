package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.beans.project;

public class Project {
    private String projectId;
    private String name;
    private String description;
    private int priority;
    private String state;
    private String createdDate;
    private String employeeId;

    public Project() {
    }

    public Project(String projectId, String name, String description, int priority, String state, String createdDate, String employeeId) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.state = state;
        this.createdDate = createdDate;
        this.employeeId = employeeId;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String toString() {
        return "Project-API: [" +
                "project-id:'" + projectId + '\'' +
                ", project-name:'" + name + '\'' +
                ", description: '" + description + '\'' +
                ", priority: " + priority +
                ", state:'" + state + '\'' +
                ", created-date='" + createdDate + '\'' +
                ", employee-assigned='" + employeeId + '\'' +
                ']';
    }
}
