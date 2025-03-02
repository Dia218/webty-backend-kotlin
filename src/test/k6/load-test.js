import {check} from "k6";
import http from "k6/http";

// 부하 테스트 옵션 설정
export let options = {
    stages: [
        {duration: '10s', target: 10}, // 10초 동안 10VU까지 증가
        {duration: '20s', target: 50}, // 20초 동안 50VU까지 증가
        {duration: '10s', target: 0},  // 10초 동안 점진적으로 감소
    ]
};

export default function () {
    let res = http.get("https://test.loadimpact.com/");
    check(res, {
        "is status 200": (r) => r.status === 200,
    });
}
