import http from 'k6/http';
import {check} from 'k6';

const webtoonBaseUrl = 'http://host.docker.internal:8080/webtoons';

export default function () {
    const response = http.get(`${webtoonBaseUrl}/fetch`); // 인증 토큰 없이 요청

    check(response, {
        'fetchWebtoons should return status 200': (r) => r.status === 200,
    });

    console.log('✅ Webtoon 데이터 가져오기');
}
