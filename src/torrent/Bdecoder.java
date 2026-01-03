package torrent;


import java.io.*;
import java.util.*;

public class Bdecoder {
    private final PushbackInputStream in;

    public Bdecoder(InputStream in) {
        // 첫 바이트를 확인하고 다시 되돌리기 위해 PushbackInputStream을 사용합니다.
        this.in = new PushbackInputStream(in);
    }

    /**
     * 스트림에서 다음 Bencode 요소를 읽어 자바 객체로 반환합니다.
     */
    public Object decode() throws IOException {
        int ch = in.read();
        if (ch == -1) return null; // 스트림 끝

        if (ch == 'i') {
            return decodeInteger();
        } else if (ch == 'l') {
            return decodeList();
        } else if (ch == 'd') {
            return decodeDictionary();
        } else if (Character.isDigit(ch)) {
            // 숫자로 시작하면 문자열(byte[])입니다. 읽은 숫자를 다시 스트림에 넣고 처리합니다.
            in.unread(ch);
            return decodeBytes();
        } else {
            throw new IOException("알 수 없는 Bencode 포맷 접두사: " + (char) ch);
        }
    }

    // 1. Integer 복원 (i...e)
    private Long decodeInteger() throws IOException {
        String s = readUntil('e');
        return Long.parseLong(s);
    }

    // 2. Byte Array 복원 (길이:내용)
    private byte[] decodeBytes() throws IOException {
        String lenStr = readUntil(':');
        int len = Integer.parseInt(lenStr);
        byte[] data = new byte[len];
        int read = 0;
        while (read < len) {
            int n = in.read(data, read, len - read);
            if (n == -1) throw new EOFException("데이터가 부족합니다.");
            read += n;
        }
        return data;
    }

    // 3. List 복원 (l...e)
    private List<Object> decodeList() throws IOException {
        List<Object> list = new ArrayList<>();
        while (true) {
            int ch = in.read();
            if (ch == 'e') break; // 리스트 끝
            in.unread(ch);
            list.add(decode()); // 재귀적으로 내부 요소 디코딩
        }
        return list;
    }

    // 4. Dictionary 복원 (d...e)
    private Map<String, Object> decodeDictionary() throws IOException {
        Map<String, Object> map = new TreeMap<>(); // 키 정렬 상태 유지를 위해 TreeMap 사용
        while (true) {
            int ch = in.read();
            if (ch == 'e') break; // 딕셔너리 끝
            in.unread(ch);

            // Bencode 규약상 키는 항상 문자열(byte[])입니다.
            byte[] keyBytes = (byte[]) decode();
            String key = new String(keyBytes, "UTF-8");
            Object value = decode();
            map.put(key, value);
        }
        return map;
    }

    // 특정 구분자(e 또는 :)가 나올 때까지 읽어서 문자열로 반환
    private String readUntil(char delimiter) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1 && ch != delimiter) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}