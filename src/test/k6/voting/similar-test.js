import {check, sleep} from 'k6';
import http from 'k6/http';

const baseUrl = 'http://host.docker.internal:8081/similar';
const token = __ENV.K6_TOKEN;

export const options = {
    vus: 1,
    iterations: 50,
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

export default function () {
    register();  // 등록
    find();      // 조회
    remove();    // 삭제
}

// ✅ 1️⃣ 유사 웹툰 등록 (50번 실행)
export function register() {
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
            createdSimilarIds.push(responseBody.similarId);
        }
    }
    sleep(0.2);
}

// ✅ 2️⃣ 유사 웹툰 조회 (50번 실행, 등록된 ID 사용)
export function find() {
    if (createdSimilarIds.length === 0) return;

    const targetWebtoonId = getNextTargetId() - 1;
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
    if (createdSimilarIds.length === 0) return;

    const similarId = createdSimilarIds.pop();
    console.log('similarId', similarId)
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

// ✅ 4️⃣ 테스트 결과 요약 (InfluxDB 저장)
export function handleSummary(data) {
    return {
        "summary.json": JSON.stringify(data, null, 2), // JSON 저장
    };
}