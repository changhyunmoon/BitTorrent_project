package tracker;

import torrent.Bdecoder;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class TrackerSeedingRequest {

    /**
     * 트래커에 Announce 요청을 보내고 응답을 받아옵니다.
     */
    public Map<String, Object> announce(String trackerUrl, byte[] infoHash, int myPort,
                                        long uploaded, long downloaded, long left, String event) throws Exception {

        // 1. Peer ID 생성 (클라이언트 식별용 20바이트)
        String peerId = "-GE3000-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 2. 모든 파라미터를 포함한 Full URL 생성
        String fullUrl = buildFullUrl(trackerUrl, infoHash, peerId, myPort, uploaded, downloaded, left, event);

        // 3. 요청 정보 출력 (디버깅용)
        printSeedingRequest(fullUrl, infoHash, peerId, myPort, uploaded, downloaded, left, event);

        // 4. HTTP GET 요청 수행
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("트래커 응답 실패: HTTP " + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                byte[] responseBytes = in.readAllBytes();
                return Bdecoder.decode(responseBytes);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 모든 비트토렌트 필수 파라미터를 URL에 조립합니다.
     */
    private String buildFullUrl(String baseUrl, byte[] infoHash, String peerId, int port,
                                long uploaded, long downloaded, long left, String event) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");

        // info_hash: 바이너리이므로 % 인코딩 필수
        sb.append("info_hash=").append(byteArrayToUrlEncoded(infoHash));

        // peer_id: URL 인코딩
        sb.append("&peer_id=").append(URLEncoder.encode(peerId, StandardCharsets.UTF_8));

        // 포트 및 전송 통계
        sb.append("&port=").append(port);
        sb.append("&uploaded=").append(uploaded);
        sb.append("&downloaded=").append(downloaded);
        sb.append("&left=").append(left);

        // 이벤트 상태 (started, stopped, completed)
        if (event != null && !event.isEmpty()) {
            sb.append("&event=").append(event);
        }

        // 추가 옵션
        sb.append("&compact=1"); // 피어 목록을 바이너리(6바이트 단위)로 요청
        sb.append("&numwant=50"); // 요청할 피어 수

        return sb.toString();
    }

    private String byteArrayToUrlEncoded(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append('%');
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 요청 내용을 시각적으로 출력합니다.
     */
    public void printSeedingRequest(String fullUrl, byte[] infoHash, String peerId,
                                    int port, long uploaded, long downloaded, long left, String event) {
        System.out.println("\n========== [OUT] Tracker Announce Request ==========");
        System.out.println("▶ Tracker URL  : " + fullUrl.split("\\?")[0]);
        System.out.println("▶ Info Hash    : " + infoHash);
        System.out.println("▶ Peer ID      : " + peerId);
        System.out.println("▶ Port         : " + port);
        System.out.println("▶ Uploaded     : " + uploaded + " bytes");
        System.out.println("▶ Downloaded   : " + downloaded + " bytes");
        System.out.println("▶ Left         : " + left + " bytes");
        System.out.println("▶ Event        : " + (event == null ? "none" : event));
        System.out.println("====================================================\n");
    }


}