import {check, sleep} from 'k6';
import http from 'k6/http';

const baseUrl = 'http://host.docker.internal:8081/vote';
const token = __ENV.K6_TOKEN;

export const options = {
    scenarios: {
        vote: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 50,
            startTime: '0s',
        },
        cancel: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 50,
            startTime: '10s',
        },
    },
};

// `similar` 데이터가 미리 생성되어야 하므로, 유사 웹툰 ID 목록을 저장
const createdSimilarIds = [];

export function setup() {
    // 테스트를 위해 Similar 데이터가 미리 생성되어야 함. 생성된 Similar ID 배열을 반환
    return {createdSimilarIds};
}

// ✅ 1️⃣ 유사 웹툰 투표 (50번 실행)
export function vote(data) {
    if (data.createdSimilarIds.length === 0) return; // Similar 데이터가 없다면 실행하지 않음

    const similarId = data.createdSimilarIds[Math.floor(Math.random() * data.createdSimilarIds.length)];
    const voteType = Math.random() > 0.5 ? 'like' : 'dislike'; // like 또는 dislike 중 랜덤 선택

    const res = http.post(`${baseUrl}/${similarId}?voteType=${voteType}`, null, {
        headers: {
            'Authorization': `Bearer ${token}`,
        },
    });

    check(res, {
        'vote should return status 200': (r) => r.status === 200,
    });
    sleep(0.2);
}

// ✅ 2️⃣ 유사 웹툰 투표 취소 (50번 실행)
export function cancel(data) {
    if (data.createdSimilarIds.length === 0) return; // Similar 데이터가 없다면 실행하지 않음

    const similarId = data.createdSimilarIds[Math.floor(Math.random() * data.createdSimilarIds.length)];

    const res = http.del(`${baseUrl}/${similarId}?page=0&size=10`, null, {
        headers: {
            'Authorization': `Bearer ${token}`,
        },
    });

    check(res, {
        'cancel should return status 200': (r) => r.status === 200,
    });
    sleep(0.2);
}
