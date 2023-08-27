package pg.paymentgateway.dto;

@lombok.Data
public class Data {

    private String tid; // PG거래번호
    private String tradeDateTime; // 거래일시
    private String totalAmount; // 총금액
    private String respCode; // 응답 코드
    private String respMessage; // 응답 메세지
    private String payload; // 가맹점 데이터
    private String issuerCardType; // 발급사타입
    private String issuerCardName; // 발급사명
    private String purchaseCardType; // 매입사타입
    private String purchaseCardName; // 매입사명
    private String approvalNumb; // 승인번호
    private String cardNumb; //카드번호
    private String expiryDate; // 유효기간
    private String installMonth; // 할부개월수
    private String cardType; // 카드 타입
    private String partCancelYn; // 부분취소가능여부
    private String billingToken;
}
