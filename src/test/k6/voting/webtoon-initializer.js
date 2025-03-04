import http from 'k6/http';
import {check} from 'k6';

const webtoonBaseUrl = 'http://host.docker.internal:8080/webtoons';

export default function () {
    const response = http.get(`${webtoonBaseUrl}/fetch`, {
        timeout: '600s',  // 타임아웃 시간 설정 (기본 타임아웃 시간 방지)
    });

    check(response, {
        'fetchWebtoons should return status 200': (r) => r.status === 200,
    });

    console.log('✅ Webtoon 데이터 가져오기');
}
