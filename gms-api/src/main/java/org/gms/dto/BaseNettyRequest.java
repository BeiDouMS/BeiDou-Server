package org.gms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseNettyRequest<T> {
    private String requestId;
    private String requestKey;
    private T requestData;
}
