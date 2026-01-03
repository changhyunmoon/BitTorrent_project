package torrent;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class InfoHashCalculator {

    /**
     * info 딕셔너리 맵을 받아 20바이트 SHA-1 info_hash를 계산합니다.
     */
    public static byte[] calculateInfoHash(Map<String, Object> infoMap) throws Exception {
        // 1. info 섹션만 다시 Bencode로 인코딩하여 바이트 배열 생성
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bencoder.encode(infoMap, baos);
        byte[] bencodedInfo = baos.toByteArray();

        // 2. 인코딩된 바이트 배열에 대해 SHA-1 해시 수행
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(bencodedInfo);
    }

    /**
     * 바이트 배열의 info_hash를 트래커 URL용 Hex 문자열로 변환 (확인용)
     */
    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}