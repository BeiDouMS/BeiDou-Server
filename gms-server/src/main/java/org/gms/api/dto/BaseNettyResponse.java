package org.gms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseNettyResponse<T> {
    private String responseId;
    private String responseCode;
    private String responseMessage;
    private T responseData;
}
