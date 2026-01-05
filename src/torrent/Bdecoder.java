package torrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Bdecoder {

    private final PushbackInputStream in;

    public Bdecoder(InputStream in){
        this.in = new PushbackInputStream(in);
    }

    public Object decode() throws IOException {
        int c = in.read();
        if (c == -1) return null;

        if (c == 'i') {
            return decodeInteger();
        } else if (c == 'l') {
            return decodeList();
        } else if (c == 'd') {
            return decodeDictionary();
        } else if (Character.isDigit(c)) {
            in.unread(c); // 숫자는 길이를 알아내기 위해 다시 되돌림
            return decodeBytes();
        }
        throw new IOException("알 수 없는 데이터 타입입니다: " + (char) c);
    }

    // 정수 디코딩 (i123e -> 123)
    private Long decodeInteger() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != 'e') {
            sb.append((char) c);
        }
        return Long.parseLong(sb.toString());
    }

    // 바이트 배열/문자열 디코딩 (5:hello -> hello)
    private byte[] decodeBytes() throws IOException {
        StringBuilder lengthStr = new StringBuilder();
        int c;
        while ((c = in.read()) != ':') {
            lengthStr.append((char) c);
        }
        int length = Integer.parseInt(lengthStr.toString());
        byte[] data = new byte[length];
        in.read(data);
        return data;
    }

    // 리스트 디코딩 (l...e -> List)
    private List<Object> decodeList() throws IOException {
        List<Object> list = new ArrayList<>();
        while (true) {
            int c = in.read();
            if (c == 'e') break; // 리스트 끝
            in.unread(c);
            list.add(decode());
        }
        return list;
    }

    // 딕셔너리 디코딩 (d...e -> Map)
    private Map<String, Object> decodeDictionary() throws IOException {
        Map<String, Object> map = new TreeMap<>(); // 키 정렬 보장을 위해 TreeMap 사용
        while (true) {
            int c = in.read();
            if (c == 'e') break;
            in.unread(c);

            // 키는 무조건 문자열(byte[])이므로 디코딩 후 String으로 변환
            String key = new String(decodeBytes(), StandardCharsets.UTF_8);
            Object value = decode();
            map.put(key, value);
        }
        return map;
    }
}
