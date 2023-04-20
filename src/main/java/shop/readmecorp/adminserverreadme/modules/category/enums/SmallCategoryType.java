package shop.readmecorp.adminserverreadme.modules.category.enums;

//TODO 수정필요
public enum SmallCategoryType {

    // 자기계발
    성공_처세("성공 / 처세"),
    인간_관계("인간 관계"),
    화술_협상("화술 / 협상"),
    시간_관리("시간 관리"),
    리더십("리더십"),

    // 에세이
    여행_에세이("여행 에세이"),
    예술_에세이("예술 에세이"),
    독서_에세이("독서 에세이"),
    심리_에세이("심리 에세이"),
    사랑_연애_에세이("사랑/연애 에세이"),

    // 인문
    일반("일반"),
    심리학("심리학"),
    교육학("교육학"),
    철학("철학"),
    문학이론("문학이론"),

    // 경영
    경영일반("경영일반"),
    경영이론("경영이론"),
    마케팅_광고("마케팅 / 광고"),
    제테크_금융("제테크 / 금융"),
    세계_경제("세계 경제"),

    // 언어
    영어("영어"),
    일본어("일본어"),
    중국어("중국어"),
    프랑스어("프랑스어"),
    기타_외국어("기타 외국어"),

    // 소설
    고전("고전"),
    스릴러("스릴러"),
    역사("역사"),
    SF("SF"),
    로맨스("로맨스"),

    // 역사
    한국사("한국사"),
    세계사("세계사"),
    서양사("서양사"),
    동양사("동양사"),
    신화("신화");

    private final String name;

    SmallCategoryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

//    // 자기계발
//    성공_처세, 인간_관계, 화술_협상, 시간_관리, 리더십,
//
//    // 에세이
//    여행_에세이, 예술_에세이, 독서_에세이, 심리_에세이, 사랑_연애_에세이,
//
//    // 인문
//    일반, 심리학, 교육학, 철학, 문학이론,
//
//    // 경영
//    경영일반, 경영이론, 마케팅_광고, 제테크_금융, 세계_경제,
//
//    // 언어
//    영어, 일본어, 중국어, 프랑스어, 기타_외국어,
//
//    // 소설
//    고전, 스릴러, 역사, SF, 로맨스,
//
//    // 역사
//    한국사, 세계사, 서양사, 동양사, 신화
}
