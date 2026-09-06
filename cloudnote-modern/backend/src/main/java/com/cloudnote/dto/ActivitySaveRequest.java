package com.cloudnote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivitySaveRequest {

    /** 为空=新建; 非空=修改 */
    private String activityId;

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 200, message = "活动标题过长")
    private String title;

    private String body;

    /** 结束时间毫秒时间戳, 空=长期有效 */
    private Long endTime;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
