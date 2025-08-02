package com.cluvy.auth.dto;

/*Access Token 검증 결과를 클라이언트에 응답하는 데이터*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private Long userId;
}
