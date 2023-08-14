package pg.paymentgateway.dto;

import lombok.Data;

@Data
public class KsnetResponse {

    private String aid; // API 요청 고유값
    private String code; // API 응답 코드
    private String message; // API 응답 메세지
    private pg.paymentgateway.dto.Data data;
}
