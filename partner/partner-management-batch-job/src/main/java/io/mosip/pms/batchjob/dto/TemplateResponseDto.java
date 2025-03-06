package io.mosip.pms.batchjob.dto;

import lombok.Data;
import java.util.List;

@Data
public class TemplateResponseDto {

    private List<TemplateDto> templates;

    @Data
    public static class TemplateDto {
        private String id;
        private String name;
        private String description;
        private String fileFormatCode;
        private String model;
        private String fileText;
        private String moduleId;
        private String moduleName;
        private String templateTypeCode;
        private String langCode;
        private boolean isActive;
    }
}
