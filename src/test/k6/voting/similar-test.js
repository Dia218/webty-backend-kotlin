import {check, sleep} from 'k6';
import http from 'k6/http';

const baseUrl = 'http://host.docker.internal:8081/similar';
const token = __ENV.K6_TOKEN;

export const options = {
    scenarios: {
        register: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 50,
            startTime: '0s',
        },
        find: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 50,
            startTime: '10s',
        },
        remove: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 50,
            startTime: '20s',
        },
    },
};

let targetIdCounter = 1;
let choiceIdCounter = 101;

function getNextTargetId() {
    const id = targetIdCounter;
    targetIdCounter = targetIdCounter >= 50 ? 1 : targetIdCounter + 1;
    return id;
}

function getNextChoiceId() {
    const id = choiceIdCounter;
    choiceIdCounter = choiceIdCounter >= 150 ? 101 : choiceIdCounter + 1;
    return id;
}

// 등록된 ID들을 저장할 배열
const createdSimilarIds = [];

export function setup() {
    return {createdSimilarIds};
}

export default function (data) {
    register(data);  // 등록
    find(data);      // 조회
    remove(data);    // 삭제
}

// ✅ 1️⃣ 유사 웹툰 등록 (50번 실행)
export function register(data) {
    const targetWebtoonId = getNextTargetId();
    const choiceWebtoonId = getNextChoiceId();

    const res = http.post(`${baseUrl}`, JSON.stringify({
        targetWebtoonId,
        choiceWebtoonId,
    }), {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    });

    check(res, {
        'createSimilar should return status 200': (r) => r.status === 200,
        'createSimilar should return a valid response body': (r) => r.body.includes('similarId'),
    });

    if (res.status === 200) {
        const responseBody = JSON.parse(res.body);
        if (responseBody.similarId) {
            data.createdSimilarIds.push(responseBody.similarId);
        }
    }
    sleep(0.2);
}

// ✅ 2️⃣ 유사 웹툰 조회 (50번 실행, 등록된 ID 사용)
export function find(data) {
    if (data.createdSimilarIds.length === 0) return;

    const targetWebtoonId = getNextTargetId();
    const res = http.get(`${baseUrl}?targetWebtoonId=${targetWebtoonId}&page=0&size=10`, {
        headers: {
            'Authorization': `Bearer ${token}`,
        },
    });

    check(res, {
        'findAll should return status 200': (r) => r.status === 200,
        'findAll should return a list of similars': (r) => JSON.parse(r.body).content.length > 0,
    });
    sleep(0.2);
}

// ✅ 3️⃣ 유사 웹툰 삭제 (50번 실행, 등록된 ID 사용)
export function remove(data) {
    if (data.createdSimilarIds.length === 0) return;

    const similarId = data.createdSimilarIds.pop();
    const res = http.del(`${baseUrl}/${similarId}`, null, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    });

    check(res, {
        'deleteSimilar should return status 200': (r) => r.status === 200,
    });
    sleep(0.2);
}
