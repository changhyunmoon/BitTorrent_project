package tracker;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TrackerSeedingResponse {
    /**
     * 트래커 응답 결과를 분석하고 시각화합니다.
     */
    public void processResponse(Map<String, Object> response) {
        System.out.println("\n========== [IN] Tracker Announce Response ==========");

        // 1. 에러 체크 (failure reason 키가 있으면 요청 실패)
        if (response.containsKey("failure reason")) {
            String reason = parseBencodedString(response.get("failure reason"));
            System.err.println("❌ 등록 실패: " + reason);
            System.out.println("====================================================");
            return;
        }

        // 2. 성공 시 기본 정보 출력
        System.out.println("✅ 등록 성공!");
        System.out.println("▶ Interval (재요청 주기) : " + response.get("interval") + "초");

        if (response.containsKey("complete"))
            System.out.println("▶ Seeders (완료자 수)    : " + response.get("complete"));
        if (response.containsKey("incomplete"))
            System.out.println("▶ Leechers (미완료자 수) : " + response.get("incomplete"));

        // 3. 피어 리스트 파싱 (compact=1 방식 대응)
        if (response.containsKey("peers")) {
            Object peersObj = response.get("peers");
            System.out.println("▶ Peers (연결 가능 피어) :");

            if (peersObj instanceof byte[]) {
                // 바이너리 데이터(6바이트 단위) 파싱
                parseBinaryPeers((byte[]) peersObj);
            } else {
                // 딕셔너리 리스트 형태일 경우 (일부 트래커)
                System.out.println("   " + peersObj);
            }
        }
        System.out.println("====================================================\n");
    }

    /**
     * 6바이트 단위의 바이너리 피어 데이터를 IP:Port로 변환하여 출력
     */
    private void parseBinaryPeers(byte[] peers) {
        if (peers.length % 6 != 0) {
            System.out.println("   [데이터 오류: 피어 바이트 길이가 올바르지 않습니다.]");
            return;
        }

        for (int i = 0; i < peers.length; i += 6) {
            // IP 주소 파싱 (4바이트)
            String ip = String.format("%d.%d.%d.%d",
                    peers[i] & 0xFF, peers[i+1] & 0xFF, peers[i+2] & 0xFF, peers[i+3] & 0xFF);

            // Port 번호 파싱 (2바이트, Big-Endian)
            int port = ((peers[i+4] & 0xFF) << 8) | (peers[i+5] & 0xFF);

            System.out.println("   - IP: " + ip + " | Port: " + port);
        }
    }

    /**
     * Bdecoder로부터 받은 byte[] 또는 String을 안전하게 문자열로 변환
     */
    private String parseBencodedString(Object obj) {
        if (obj instanceof byte[]) {
            return new String((byte[]) obj, StandardCharsets.UTF_8);
        }
        return String.valueOf(obj);
    }
}
