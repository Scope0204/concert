## 프로젝트 개요
콘서트 좌석을 예매하고 결제할 수 있는 시스템을 구현합니다.
<br>
<details>
  <summary> 주요 기능 및 세부 기능 사항은 다음과 같습니다. </summary>

  ## 1. 주요 기능
  ### 1.1. 대기열 시스템
  - 사용자는 서비스 접근 전 대기열에 진입
  - 대기열 순서에 따라 서비스 이용 권한 부여
  - 대기열 토큰을 통해 사용자 식별 및 접근 권한 관리
  ### 1.2. 좌석 예약
  - 사용자는 예약 가능한 날짜와 좌석 조회
  - 원하는 좌석 선택 및 임시 예약 (5분간 유효)
  - 임시 예약 상태에서 다른 사용자의 해당 좌석 접근 제한
  ### 1.3. 결제 시스템
  - 사용자는 잔액 충전 가능
  - 예약한 좌석에 대해 충전된 잔액으로 결제
  - 결제 완료 시 좌석 소유권 확정 및 대기열 토큰 만료

  ## 2. 세부 기능
  ### 2.1. 유저 토큰 발급 API
  - 기능: 서비스 이용을 위한 토큰 발급
  - 포함 정보: 유저 UUID, 대기 순서 또는 잔여 시간
  - 유효 기간 설정 및 관리
  ### 2.2. 예약 가능 날짜/좌석 API
  - 예약 가능한 날짜 목록 조회 기능
  - 특정 날짜의 예약 가능한 좌석 정보 조회 기능
  - 좌석 번호는 1~50까지 관리
  ### 2.3. 좌석 예약 요청 API
  - 날짜와 좌석 정보를 입력받아 예약 처리
  - 임시 배정 기능 (약 5분간)
  - 배정 시간 내 결제 미완료 시 자동 해제
  ### 2.4. 잔액 충전/조회 API
  - 사용자별 잔액 충전 기능
  - 현재 잔액 조회 기능
  ### 2.5. 결제 API
  - 선택한 좌석에 대한 결제 처리
  - 결제 완료 시 좌석 소유권 확정
  - 결제 완료 후 대기열 토큰 만료 처리

</details>
  
## 프로젝트 문서
### [마일스톤 설정](https://github.com/users/Scope0204/projects/1)
### [시퀀스 다이어그램](https://github.com/Scope0204/concert/blob/master/docs/concert_sequence_diagram.md) 
### [ERD 설계](https://github.com/Scope0204/concert/blob/master/docs/concert_erd.mmd) 
### [API 명세서](https://github.com/Scope0204/concert/blob/master/docs/concert_swagger_docs.md)
### [콘서트 예약 서비스 동시성 이슈 파악 및 대응 방법](https://github.com/Scope0204/concert/blob/master/docs/concert_concurrency_report.md)
### [인덱스 도입을 통한 성능 개선](https://github.com/Scope0204/concert/blob/master/docs/concert_index.md)
