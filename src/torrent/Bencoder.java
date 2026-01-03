package torrent;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Bencoder {

    /**
     * 자바 객체를 Bencode 형식으로 인코딩하여 OutputStream에 씁니다.
     */
    public static void encode(Object obj, OutputStream out) throws IOException {
        if (obj instanceof String) {
            encodeString((String) obj, out);
        } else if (obj instanceof byte[]) {
            encodeBytes((byte[]) obj, out);
        } else if (obj instanceof Number) {
            encodeInteger(((Number) obj).longValue(), out);
        } else if (obj instanceof List) {
            encodeList((List<?>) obj, out);
        } else if (obj instanceof Map) {
            encodeDictionary((Map<String, ?>) obj, out);
        } else {
            throw new IllegalArgumentException("지원하지 않는 데이터 타입입니다: " + obj.getClass());
        }
    }

    // 1. String 처리 (길이:내용)
    private static void encodeString(String s, OutputStream out) throws IOException {
        encodeBytes(s.getBytes(StandardCharsets.UTF_8), out);
    }

    // 2. Byte Array 처리 (이미지, 해시값 등 바이너리 데이터용)
    private static void encodeBytes(byte[] bytes, OutputStream out) throws IOException {
        out.write(Integer.toString(bytes.length).getBytes(StandardCharsets.UTF_8));
        out.write(':');
        out.write(bytes);
    }

    // 3. Integer 처리 (i숫자e)
    private static void encodeInteger(long n, OutputStream out) throws IOException {
        out.write('i');
        out.write(Long.toString(n).getBytes(StandardCharsets.UTF_8));
        out.write('e');
    }

    // 4. List 처리 (l요소1요소2e)
    private static void encodeList(List<?> list, OutputStream out) throws IOException {
        out.write('l');
        for (Object item : list) {
            encode(item, out);
        }
        out.write('e');
    }

    // 5. Dictionary 처리 (d키1값1키2값2e) - 키 정렬 필수!
    private static void encodeDictionary(Map<String, ?> map, OutputStream out) throws IOException {
        out.write('d');
        // Bencode 표준: 키는 반드시 사전순 정렬되어야 함
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            encodeString(key, out);      // 키는 항상 문자열
            encode(map.get(key), out);   // 값은 재귀적으로 인코딩
        }
        out.write('e');
    }
}