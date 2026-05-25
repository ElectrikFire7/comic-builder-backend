package com.fullcomicbuilder.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    /** References users._id */
    private String userId;

    private String projectName;

    /** GCS path prefix, e.g. "username/MyProject" */
    private String storageFolderPath;
}
