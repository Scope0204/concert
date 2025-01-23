import http from "k6/http";
import { check, sleep } from "k6";

// export const options = {
//   vus: 1, // 가상 사용자 수
//   duration: "1m", // 테스트 지속 시간
// };

export const options = {
  stages: [
    { duration: "5m", target: 50 }, // Load Test: 50명의 가상 사용자로 5분 동안 테스트
    { duration: "2m", target: 200 }, // Peak Test: 200명의 가상 사용자로 2분 동안 피크 부하 테스트
  ],
};

export default function () {
  const userId = 1;

  // 요청 헤더 설정
  const headers = {
    "User-Id": userId.toString(),
    accept: "*/*",
  };

  // POST 요청 보내기
  const response = http.post("http://concert-app:80/queue/token/users/", "", {
    headers: headers,
  });

  // 응답 체크
  check(response, {
    "status is 200": (r) => r.status === 200, // 상태 코드 200 확인
    "response has token": (r) => {
      const body = JSON.parse(r.body);
      return body.token !== undefined; // 응답에서 token 존재 여부 확인
    },
  });

  // token 출력
  if (response.status === 200) {
    const token = JSON.parse(response.body).token;
    console.log(`User ${userId} received token: ${token}`);
  }

  // 1초 대기
  sleep(1);
}
