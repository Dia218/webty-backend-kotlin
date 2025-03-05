import {check, sleep} from 'k6';
import http from 'k6/http';

const baseUrl = 'http://host.docker.internal:8081/similar';
const token = __ENV.K6_TOKEN;

export const options = {
    vus: 1,
    iterations: 50,
    startTime: '0s',
};

let idCounter = 1;

function getNextTargetId() {
    const id = idCounter;
    idCounter = idCounter >= 50 ? 1 : idCounter + 1;  // 1부터 50까지 순차적으로 사용
    return id;
}

function getNextChoiceId() {
    const id = idCounter + 100;  // 101부터 150까지 순차적으로 사용
    idCounter = idCounter >= 50 ? 1 : idCounter + 1;  // 1부터 50까지 순차적으로 사용
    return id;
}

export function setup() {
    // 테스트 시작 전 초기화될 필요가 있는 데이터
    return {
        createdSimilarIds: [],
    };
}

// ✅ Similar 데이터 생성 (50개)
export function createSimilar(data) {
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
